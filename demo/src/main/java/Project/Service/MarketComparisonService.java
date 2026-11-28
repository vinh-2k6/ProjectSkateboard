package Project.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import Project.Model.DTO.Response.MarketComparisonResponseDTO;
import Project.Model.DTO.Response.MarketplaceOfferDTO;
import Project.Model.DTO.Response.ProductResponseDTO;
import Project.Model.Entity.MarketplaceListing;
import Project.Model.Enum.MarketplacePlatform;
import Project.Repository.MarketplaceListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketComparisonService {

    private static final long CACHE_TTL_MS = 10 * 60 * 1000L;

    private static final String DISCLAIMER_CATALOG =
            "Giá ProjectSkateBoard lấy đúng trong kho của shop. Giá Shopee, Lazada, Tiki lấy từ bản ghi đã lưu, có thể chưa phải giá đang bán. Voucher, phí ship và flash sale làm giá thực tế thay đổi — hãy mở link để kiểm tra.";

    private static final String DISCLAIMER_ESTIMATE =
            "Giá ProjectSkateBoard lấy đúng trong kho của shop. Các sàn còn lại chưa có bản ghi giá: khoảng giá (nếu có) là ước lượng của AI, không phải giá realtime. Bấm link để xem giá, voucher và shop đang bán.";

    private static final List<String> NOISE_PHRASES = List.of(
            "so sánh giá", "so sanh gia", "so sánh", "so sanh",
            "đối chiếu", "doi chieu", "giá thị trường", "gia thi truong",
            "shopee", "lazada", "tiki",
            "các sàn", "cac san", "sàn khác", "san khac",
            "giúp mình", "giup minh", "cho mình", "cho minh",
            "sản phẩm này", "san pham nay", "đôi này", "doi nay",
            "giày này", "giay nay",
            "với", "voi", "trên", "giữa", "giua", "và", "va", "hoặc", "hoac",
            "giá", "gia", "ở", "o", "nhé", "nhe", "giúp", "giup", "mình", "minh",
            "marketplace", "shop"
    );

    private final ProductService productService;
    private final MarketplaceListingRepository listingRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public MarketComparisonResponseDTO compare(Integer productId, String userMessage) {
        ProductResponseDTO product = resolveProduct(productId, userMessage);
        if (product == null) {
            return MarketComparisonResponseDTO.builder()
                    .resolved(false)
                    .summary("Mình cần đúng một mẫu trên ProjectSkateBoard để đối chiếu. Bạn mở trang sản phẩm rồi bấm So sánh sàn, hoặc nhắn tên mẫu, ví dụ: \"So sánh Nike Dunk với Shopee, Lazada, Tiki và TikTok Shop\".")
                    .offers(List.of())
                    .build();
        }

        List<MarketplaceListing> listings = listingRepository.findByProduct_Id(product.getId());
        String cacheKey = cacheKey(product.getId(), listings);
        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && cached.expiresAt > System.currentTimeMillis()) {
            return cached.value;
        }

        List<MarketplaceOfferDTO> offers = buildOffers(product, listings);
        boolean hasCatalog = offers.stream().anyMatch(offer -> "CATALOG".equals(offer.getSource()));
        String disclaimer = hasCatalog ? DISCLAIMER_CATALOG : DISCLAIMER_ESTIMATE;

        String summary = fallbackSummary(product, hasCatalog);
        String verdict = hasCatalog ? "Đối chiếu theo giá đã lưu" : "Mở sàn để xem giá thật";

        try {
            String raw = geminiService.chat(buildPrompt(product, offers, userMessage, hasCatalog));
            JsonNode root = parseJson(raw);
            if (root != null) {
                String aiSummary = root.path("summary").asText("").trim();
                String aiVerdict = root.path("verdict").asText("").trim();
                if (!aiSummary.isBlank()) {
                    summary = aiSummary;
                }
                if (!aiVerdict.isBlank()) {
                    verdict = aiVerdict;
                }
                mergePlatformNotes(offers, root.path("platforms"), product.getPrice(), hasCatalog);
            }
        } catch (Exception e) {
            log.warn("Không gọi được AI để phân tích so sánh sàn: {}", e.getMessage());
        }

        MarketComparisonResponseDTO response = MarketComparisonResponseDTO.builder()
                .resolved(true)
                .summary(summary)
                .verdict(verdict)
                .disclaimer(disclaimer)
                .product(product)
                .offers(offers)
                .build();
        cache.put(cacheKey, new CacheEntry(response, System.currentTimeMillis() + CACHE_TTL_MS));
        return response;
    }

    private ProductResponseDTO resolveProduct(Integer productId, String userMessage) {
        if (productId != null) {
            return productService.fetchProductById(productId);
        }
        String keyword = extractKeyword(userMessage);
        if (keyword == null) {
            return null;
        }
        List<ProductResponseDTO> found = productService.searchProducts(keyword, null, null, null, null);
        if (found == null || found.isEmpty()) {
            return null;
        }
        return found.get(0);
    }

    String extractKeyword(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String cleaned = message.toLowerCase(Locale.ROOT);
        for (String phrase : NOISE_PHRASES) {
            cleaned = cleaned.replace(phrase, " ");
        }
        cleaned = cleaned.replaceAll("[^\\p{L}\\p{N}\\s]", " ").replaceAll("\\s+", " ").trim();
        if (cleaned.length() < 2) {
            return null;
        }
        return cleaned;
    }

    private List<MarketplaceOfferDTO> buildOffers(ProductResponseDTO product, List<MarketplaceListing> listings) {
        Map<MarketplacePlatform, MarketplaceListing> byPlatform = new LinkedHashMap<>();
        for (MarketplaceListing listing : listings) {
            if (listing.getPlatform() != null) {
                byPlatform.putIfAbsent(listing.getPlatform(), listing);
            }
        }

        List<MarketplaceOfferDTO> offers = new ArrayList<>();
        String stock = (product.getQuantity() != null && product.getQuantity() > 0)
                ? "Còn " + product.getQuantity() + " sản phẩm trong kho shop."
                : "Đang tạm hết hàng trên shop.";
        offers.add(MarketplaceOfferDTO.builder()
                .platform("PROJECT_SKATEBOARD")
                .platformLabel("ProjectSkateBoard")
                .price(product.getPrice())
                .shopName("ProjectSkateBoard")
                .listingTitle(product.getName())
                .url("/products/" + product.getId())
                .highlights("Giá niêm yết của shop. " + stock)
                .source("STORE")
                .ours(true)
                .build());

        for (MarketplacePlatform platform : MarketplacePlatform.values()) {
            MarketplaceListing listing = byPlatform.get(platform);
            if (listing != null && listing.getPrice() != null) {
                offers.add(MarketplaceOfferDTO.builder()
                        .platform(platform.name())
                        .platformLabel(platform.getLabel())
                        .price(listing.getPrice())
                        .rating(listing.getRating())
                        .soldCount(listing.getSoldCount())
                        .shopName(listing.getShopName())
                        .listingTitle(listing.getListingTitle() != null ? listing.getListingTitle() : product.getName())
                        .url(listing.getProductUrl() != null && !listing.getProductUrl().isBlank()
                                ? listing.getProductUrl()
                                : platform.searchUrl(product.getName()))
                        .highlights("Giá lấy từ dữ liệu sàn đã lưu trong hệ thống.")
                        .source("CATALOG")
                        .ours(false)
                        .build());
            } else {
                offers.add(MarketplaceOfferDTO.builder()
                        .platform(platform.name())
                        .platformLabel(platform.getLabel())
                        .listingTitle(product.getName())
                        .url(platform.searchUrl(product.getName()))
                        .highlights("Chưa có bản ghi giá. Mở sàn để xem giá và voucher hiện tại.")
                        .source("SEARCH")
                        .ours(false)
                        .build());
            }
        }
        return offers;
    }

    private void mergePlatformNotes(List<MarketplaceOfferDTO> offers, JsonNode platforms, Integer ourPrice, boolean hasCatalog) {
        if (platforms == null || !platforms.isArray()) {
            return;
        }
        Map<String, JsonNode> byName = new LinkedHashMap<>();
        for (JsonNode node : platforms) {
            String platform = node.path("platform").asText("").trim().toUpperCase(Locale.ROOT);
            if (!platform.isBlank()) {
                byName.put(platform, node);
            }
        }
        for (MarketplaceOfferDTO offer : offers) {
            if (offer.isOurs()) {
                continue;
            }
            JsonNode node = byName.get(offer.getPlatform());
            if (node == null) {
                continue;
            }
            String highlights = node.path("highlights").asText("").trim();
            if (!highlights.isBlank()) {
                offer.setHighlights(highlights);
            }
            if ("CATALOG".equals(offer.getSource()) || hasCatalog && offer.getPrice() != null) {
                continue;
            }
            Integer min = readPrice(node.get("priceMin"));
            Integer max = readPrice(node.get("priceMax"));
            if (min == null || max == null || ourPrice == null) {
                continue;
            }
            if (min > max) {
                Integer swap = min;
                min = max;
                max = swap;
            }
            int lowBound = (int) Math.round(ourPrice * 0.35);
            int highBound = ourPrice * 4;
            if (min < lowBound || max > highBound || max - min > ourPrice * 2) {
                continue;
            }
            offer.setPriceMin(min);
            offer.setPriceMax(max);
            offer.setSource("ESTIMATE");
        }
    }

    private Integer readPrice(JsonNode node) {
        if (node == null || node.isNull() || !node.isNumber()) {
            return null;
        }
        long value = node.asLong();
        if (value <= 0 || value > Integer.MAX_VALUE) {
            return null;
        }
        return (int) value;
    }

    private String buildPrompt(
            ProductResponseDTO product,
            List<MarketplaceOfferDTO> offers,
            String userMessage,
            boolean hasCatalog) {
        String brand = product.getBrand() != null ? product.getBrand().getName() : "ProjectSkateBoard";
        String category = product.getCategory() != null ? product.getCategory().getName() : "Không rõ";
        StringBuilder rows = new StringBuilder();
        for (MarketplaceOfferDTO offer : offers) {
            rows.append("- ")
                    .append(offer.getPlatform())
                    .append(" | nguồn=").append(offer.getSource())
                    .append(" | giá=").append(offer.getPrice() == null ? "không có" : offer.getPrice())
                    .append(" | shop=").append(offer.getShopName() == null ? "" : offer.getShopName())
                    .append(" | đánh giá=").append(offer.getRating() == null ? "" : offer.getRating())
                    .append(" | đã bán=").append(offer.getSoldCount() == null ? "" : offer.getSoldCount())
                    .append('\n');
        }

        String priceRule = hasCatalog
                ? "Đã có giá CATALOG. Không được đổi các con số đó và không bịa thêm giá exact cho sàn chưa có."
                : "Chưa có giá sàn ngoài. Không được đưa một mức giá exact. Nếu đủ tự tin, đưa priceMin và priceMax (VND, số nguyên) trong khoảng hợp lý quanh giá shop; nếu không chắc thì để null.";

        return """
                Bạn là chuyên viên đối chiếu giá của ProjectSkateBoard.
                Khách muốn so sánh sản phẩm của shop với Shopee, Lazada, Tiki.

                SẢN PHẨM SHOP (giá này là sự thật, không được sửa):
                - Tên: %s
                - Thương hiệu: %s
                - Danh mục: %s
                - Giá: %s VND
                - Mô tả: %s

                BẢNG GIÁ ĐẦU VÀO:
                %s

                Câu hỏi khách: "%s"

                %s

                Trả về DUY NHẤT JSON:
                {
                  "summary": "4-6 câu tiếng Việt, xưng mình-bạn. Nói rõ giá shop, shop nên cân nhắc gì khi mở từng sàn (voucher, shop mall, hàng auth, phí ship, đổi trả). Không bịa thông số kỹ thuật.",
                  "verdict": "một câu ngắn, ví dụ: Giá shop đang cạnh tranh hoặc Nên mở link sàn để chốt giá",
                  "platforms": [
                    {"platform": "SHOPEE", "priceMin": null, "priceMax": null, "highlights": "một câu"},
                    {"platform": "LAZADA", "priceMin": null, "priceMax": null, "highlights": "một câu"},
                    {"platform": "TIKI", "priceMin": null, "priceMax": null, "highlights": "một câu"},
                  ]
                }
                """.formatted(
                product.getName(),
                brand,
                category,
                product.getPrice() == null ? "Liên hệ" : String.format("%,d", product.getPrice()),
                product.getDescription() == null ? "Chưa có mô tả" : product.getDescription(),
                rows,
                userMessage == null ? "So sánh giá giúp mình" : userMessage,
                priceRule
        );
    }

    private JsonNode parseJson(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return null;
        }
        String clean = rawText.trim();
        if (clean.startsWith("```json")) {
            clean = clean.substring(7);
        } else if (clean.startsWith("```")) {
            clean = clean.substring(3);
        }
        if (clean.endsWith("```")) {
            clean = clean.substring(0, clean.length() - 3);
        }
        clean = clean.trim();
        int start = clean.indexOf('{');
        int end = clean.lastIndexOf('}');
        if (start >= 0 && end > start) {
            clean = clean.substring(start, end + 1);
        }
        try {
            return objectMapper.readTree(clean);
        } catch (Exception e) {
            log.warn("Không đọc được JSON so sánh sàn: {}", e.getMessage());
            return null;
        }
    }

    private String fallbackSummary(ProductResponseDTO product, boolean hasCatalog) {
        String price = product.getPrice() == null ? "liên hệ" : String.format("%,d đ", product.getPrice());
        if (hasCatalog) {
            return "Mình đối chiếu " + product.getName() + " giá " + price
                    + " trên ProjectSkateBoard với bản ghi Shopee, Lazada, Tiki đang lưu. Bạn mở từng link để xem voucher và phí ship trước khi chốt.";
        }
        return "Mình lấy giá " + product.getName() + " trên ProjectSkateBoard là " + price
                + ". Shopee, Lazada, Tiki chưa có bản ghi giá trong hệ thống, nên mình để link tìm đúng mẫu để bạn xem giá đang bán.";
    }

    private String cacheKey(Integer productId, List<MarketplaceListing> listings) {
        StringBuilder key = new StringBuilder().append(productId);
        for (MarketplaceListing listing : listings) {
            key.append('|')
                    .append(listing.getPlatform())
                    .append(':')
                    .append(listing.getPrice());
        }
        return key.toString();
    }

    private record CacheEntry(MarketComparisonResponseDTO value, long expiresAt) {
    }
}
