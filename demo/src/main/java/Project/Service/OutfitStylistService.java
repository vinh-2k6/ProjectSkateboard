package Project.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import Project.Model.DTO.Response.OutfitItemDTO;
import Project.Model.DTO.Response.OutfitResponseDTO;
import Project.Model.DTO.Response.ProductResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutfitStylistService {

    private final ProductService productService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    /**
     * Tư vấn phối đồ hoàn chỉnh dựa trên sản phẩm (giày/ván) và yêu cầu của người dùng
     */
    public OutfitResponseDTO generateOutfit(Integer productId, String userMessage, String preferredStyle, String occasion) {
        ProductResponseDTO shoe = null;
        if (productId != null) {
            try {
                shoe = productService.fetchProductById(productId);
            } catch (Exception e) {
                log.warn("Không tìm thấy sản phẩm id={}, sẽ tạo outfit tự do: {}", productId, e.getMessage());
            }
        }

        String shoeContext = "";
        if (shoe != null) {
            shoeContext = """
                    ĐÔI GIÀY CHỦ ĐẠO CẦN PHỐI ĐỒ:
                    - Tên sản phẩm: %s
                    - Thương hiệu: %s
                    - Danh mục: %s
                    - Giá: %s VNĐ
                    - Mô tả/Đặc điểm: %s
                    """.formatted(
                    shoe.getName(),
                    shoe.getBrand() != null ? shoe.getBrand().getName() : "ProjectSkateBoard",
                    shoe.getCategory() != null ? shoe.getCategory().getName() : "Sneaker",
                    shoe.getPrice() != null ? String.format("%,d", shoe.getPrice()) : "Liên hệ",
                    shoe.getDescription() != null ? shoe.getDescription() : "Giày thể thao"
            );
        } else {
            shoeContext = "Khách hàng muốn tư vấn phối đồ phong cách thời trang thể thao / sneaker / streetwear nói chung.";
        }

        String prompt = """
                Bạn là một Fashion Stylist chuyên nghiệp hàng đầu về văn hóa Sneaker, Skater và Streetwear cho giới trẻ của ProjectSkateBoard.
                
                %s

                YÊU CẦU CỦA KHÁCH HÀNG:
                - Phong cách mong muốn: %s
                - Dịp sử dụng: %s
                - Tin nhắn của khách: "%s"

                NHIỆM VỤ:
                Hãy thiết kế một Set đồ (Outfit) hoàn hảo, hài hòa về màu sắc, phom dáng và tôn trọn vẻ đẹp của đôi giày.
                Trả về DUY NHẤT một chuỗi JSON hợp lệ (không kèm theo văn bản giải thích bên ngoài JSON) theo cấu trúc sau:
                {
                  "outfitName": "Tên set đồ thật bắt tai (ví dụ: 'Streetwear Skater Vibe', 'Clean Fit Dạo Phố', 'Sporty Chic Năng Động')",
                  "style": "Phong cách set đồ",
                  "occasion": "Dịp mặc phù hợp nhất",
                  "items": [
                    {
                      "category": "Áo",
                      "name": "Tên loại áo cụ thể (ví dụ: Áo thun oversize graphic tee, Áo hoodie nỉ bông, Áo khoác bomber...)",
                      "color": "Màu sắc đề xuất",
                      "note": "Cách chọn phom áo, chất liệu hoặc cách mặc để tôn dáng"
                    },
                    {
                      "category": "Quần",
                      "name": "Tên loại quần cụ thể (ví dụ: Quần túi hộp Cargo Pants, Quần Baggy Jeans rách gối, Quần Short dù...)",
                      "color": "Màu sắc đề xuất",
                      "note": "Độ dài, độ rộng gấu quần để khoe trọn form giày"
                    },
                    {
                      "category": "Phụ kiện",
                      "name": "Phụ kiện đi kèm (ví dụ: Mũ lưỡi trai, Kính râm, Túi đeo chéo crossbody, Tất cổ cao...)",
                      "color": "Màu sắc",
                      "note": "Điểm nhấn tạo điểm cộng cho toàn bộ set đồ"
                    }
                  ],
                  "stylingTips": "Đoạn lời khuyên chân thành và sành điệu từ stylist về cách phối màu (tone-sur-tone, tương phản), cách xắn gấu quần hoặc mẹo tạo dáng đẹp nhất với set đồ này.",
                  "imagePrompt": "A high-fashion full-body photo of a trendy young model wearing this exact outfit: [top description in English], [bottom description in English], stylish sneakers, urban street background, editorial lookbook style, cinematic lighting, 4k"
                }
                """.formatted(
                shoeContext,
                preferredStyle != null ? preferredStyle : "Tự do / Streetwear",
                occasion != null ? occasion : "Dạo phố / Đi chơi hàng ngày",
                userMessage != null ? userMessage : "Gợi ý cho mình set đồ đẹp nhất nhé"
        );

        try {
            String aiRawResponse = geminiService.chat(prompt);
            return parseOutfitJson(aiRawResponse, shoe, preferredStyle, occasion);
        } catch (Exception e) {
            log.error("Lỗi khi sinh gợi ý phối đồ từ AI: {}", e.getMessage());
            return buildFallbackOutfit(shoe, preferredStyle, occasion);
        }
    }

    private OutfitResponseDTO parseOutfitJson(String rawText, ProductResponseDTO shoe, String defaultStyle, String defaultOccasion) {
        if (rawText == null || rawText.isBlank()) {
            return buildFallbackOutfit(shoe, defaultStyle, defaultOccasion);
        }

        // Bỏ các ký tự markdown ```json và ``` nếu có
        String cleanJson = rawText.trim();
        if (cleanJson.startsWith("```json")) {
            cleanJson = cleanJson.substring(7);
        } else if (cleanJson.startsWith("```")) {
            cleanJson = cleanJson.substring(3);
        }
        if (cleanJson.endsWith("```")) {
            cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
        }
        cleanJson = cleanJson.trim();

        try {
            JsonNode root = objectMapper.readTree(cleanJson);

            String outfitName = root.path("outfitName").asText("Set Đồ Streetwear Cá Tính");
            String style = root.path("style").asText(defaultStyle != null ? defaultStyle : "Streetwear");
            String occasion = root.path("occasion").asText(defaultOccasion != null ? defaultOccasion : "Dạo phố");
            String stylingTips = root.path("stylingTips").asText("Phối đồ đồng điệu màu sắc và thoải mái di chuyển.");

            List<OutfitItemDTO> items = new ArrayList<>();
            JsonNode itemsNode = root.path("items");
            if (itemsNode.isArray()) {
                for (JsonNode itemNode : itemsNode) {
                    items.add(OutfitItemDTO.builder()
                            .category(itemNode.path("category").asText("Trang phục"))
                            .name(itemNode.path("name").asText(""))
                            .color(itemNode.path("color").asText(""))
                            .note(itemNode.path("note").asText(""))
                            .build());
                }
            }

            if (items.isEmpty()) {
                return buildFallbackOutfit(shoe, defaultStyle, defaultOccasion);
            }

            // Tạo link ảnh AI Lookbook từ Pollinations AI
            String imagePrompt = root.path("imagePrompt").asText("");
            if (imagePrompt.isBlank()) {
                imagePrompt = "Full body fashion lookbook photo of a trendy young model wearing " + style + " outfit with " + (shoe != null ? shoe.getName() : "sneakers") + ", urban aesthetic, 4k";
            }
            String imageUrl = createPollinationsUrl(imagePrompt);

            return OutfitResponseDTO.builder()
                    .outfitName(outfitName)
                    .style(style)
                    .occasion(occasion)
                    .shoe(shoe)
                    .items(items)
                    .stylingTips(stylingTips)
                    .imageUrl(imageUrl)
                    .build();

        } catch (Exception e) {
            log.warn("Không thể parse JSON phối đồ, sử dụng fallback: {}", e.getMessage());
            return buildFallbackOutfit(shoe, defaultStyle, defaultOccasion);
        }
    }

    private OutfitResponseDTO buildFallbackOutfit(ProductResponseDTO shoe, String style, String occasion) {
        String shoeName = shoe != null ? shoe.getName() : "đôi giày của bạn";
        List<OutfitItemDTO> items = List.of(
                OutfitItemDTO.builder()
                        .category("Áo")
                        .name("Áo phông Oversize Graphic Tee")
                        .color("Trắng / Đen basic")
                        .note("Form áo rộng rãi tạo phong cách phóng khoáng và thoải mái khi vận động.")
                        .build(),
                OutfitItemDTO.builder()
                        .category("Quần")
                        .name("Quần Cargo Pants ống rộng hoặc Baggy Jeans")
                        .color("Xanh Denim / Đen / Xám tiêu")
                        .note("Gấu quần buông nhẹ vừa chạm thân giày để tôn trọn kiểu dáng.")
                        .build(),
                OutfitItemDTO.builder()
                        .category("Phụ kiện")
                        .name("Mũ lưỡi trai phong cách thể thao & Túi đeo chéo")
                        .color("Đồng màu với chi tiết giày")
                        .note("Tạo điểm nhấn phong cách streetwear năng động.")
                        .build()
        );

        String fallbackPrompt = "Full body fashion lookbook of a trendy model in streetwear hoodie and cargo pants wearing skate sneakers, urban street style, 4k";
        String imageUrl = createPollinationsUrl(fallbackPrompt);

        return OutfitResponseDTO.builder()
                .outfitName("Set Đồ Phố Năng Động Cùng " + shoeName)
                .style(style != null ? style : "Streetwear")
                .occasion(occasion != null ? occasion : "Dạo phố & Đi chơi")
                .shoe(shoe)
                .items(items)
                .stylingTips("Bạn nên phối trang phục có màu sắc đồng điệu với chi tiết trên " + shoeName + " để tạo cảm giác hài hòa và ấn tượng nhất.")
                .imageUrl(imageUrl)
                .build();
    }

    private String createPollinationsUrl(String prompt) {
        try {
            String encoded = URLEncoder.encode(prompt, StandardCharsets.UTF_8);
            // Dùng model flux với tỉ lệ dọc (512x768) chuyên chụp lookbook toàn thân
            return "https://image.pollinations.ai/prompt/" + encoded + "?width=512&height=768&nologo=true&model=flux";
        } catch (Exception e) {
            return null;
        }
    }
}
