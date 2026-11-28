package Project.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import Project.Model.DTO.Response.MarketComparisonResponseDTO;
import Project.Model.DTO.Response.MarketplaceOfferDTO;
import Project.Model.DTO.Response.ProductResponseDTO;
import Project.Model.Entity.MarketplaceListing;
import Project.Model.Enum.MarketplacePlatform;
import Project.Repository.MarketplaceListingRepository;

@ExtendWith(MockitoExtension.class)
class MarketComparisonServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private MarketplaceListingRepository listingRepository;

    @Mock
    private GeminiService geminiService;

    private MarketComparisonService service;

    @BeforeEach
    void setUp() {
        service = new MarketComparisonService(
                productService,
                listingRepository,
                geminiService,
                new ObjectMapper()
        );
    }

    @Test
    void compare_shouldKeepStorePriceAndEstimateMissingPlatforms() {
        ProductResponseDTO product = sampleProduct();
        when(productService.fetchProductById(1)).thenReturn(product);
        when(listingRepository.findByProduct_Id(1)).thenReturn(List.of());
        when(geminiService.chat(anyString())).thenReturn("""
                {
                  "summary": "Giá shop là 2.500.000đ, bạn nên mở Shopee để đối chiếu voucher.",
                  "verdict": "Mở link sàn để chốt giá",
                  "platforms": [
                    {"platform": "SHOPEE", "priceMin": 2400000, "priceMax": 2800000, "highlights": "Nhiều voucher"},
                    {"platform": "LAZADA", "priceMin": 100, "priceMax": 200, "highlights": "Khoảng giá không hợp lệ"},
                    {"platform": "TIKI", "priceMin": null, "priceMax": null, "highlights": "Xem hàng chính hãng"},
                  ]
                }
                """);

        MarketComparisonResponseDTO result = service.compare(1, "So sánh với Shopee");

        assertThat(result.isResolved()).isTrue();
        assertThat(result.getSummary()).contains("2.500.000");
        assertThat(result.getOffers()).hasSize(5);

        MarketplaceOfferDTO ours = result.getOffers().get(0);
        assertThat(ours.isOurs()).isTrue();
        assertThat(ours.getPrice()).isEqualTo(2_500_000);
        assertThat(ours.getSource()).isEqualTo("STORE");

        MarketplaceOfferDTO shopee = offer(result, "SHOPEE");
        assertThat(shopee.getSource()).isEqualTo("ESTIMATE");
        assertThat(shopee.getPriceMin()).isEqualTo(2_400_000);
        assertThat(shopee.getPriceMax()).isEqualTo(2_800_000);
        assertThat(shopee.getUrl()).contains("shopee.vn");
        assertThat(shopee.getHighlights()).isEqualTo("Nhiều voucher");

        MarketplaceOfferDTO lazada = offer(result, "LAZADA");
        assertThat(lazada.getSource()).isEqualTo("SEARCH");
        assertThat(lazada.getPriceMin()).isNull();
        assertThat(lazada.getUrl()).contains("lazada.vn");
    }

    @Test
    void compare_shouldPreferSavedListingOverAiPrice() {
        ProductResponseDTO product = sampleProduct();
        MarketplaceListing listing = new MarketplaceListing();
        listing.setPlatform(MarketplacePlatform.SHOPEE);
        listing.setPrice(2_100_000);
        listing.setShopName("Sneaker Mall");
        listing.setRating(4.9);
        listing.setSoldCount(120);
        listing.setProductUrl("https://shopee.vn/nike-dunk");

        when(productService.fetchProductById(1)).thenReturn(product);
        when(listingRepository.findByProduct_Id(1)).thenReturn(List.of(listing));
        when(geminiService.chat(anyString())).thenReturn("""
                {
                  "summary": "Shopee đang lưu 2.100.000đ.",
                  "verdict": "Shopee thấp hơn giá shop trong dữ liệu đã lưu",
                  "platforms": [
                    {"platform": "SHOPEE", "priceMin": 900000, "priceMax": 950000, "highlights": "Shop Mall, 4.9 sao"}
                  ]
                }
                """);

        MarketComparisonResponseDTO result = service.compare(1, "So sánh Shopee");

        MarketplaceOfferDTO shopee = offer(result, "SHOPEE");
        assertThat(shopee.getSource()).isEqualTo("CATALOG");
        assertThat(shopee.getPrice()).isEqualTo(2_100_000);
        assertThat(shopee.getPriceMin()).isNull();
        assertThat(shopee.getShopName()).isEqualTo("Sneaker Mall");
        assertThat(shopee.getUrl()).isEqualTo("https://shopee.vn/nike-dunk");
        assertThat(shopee.getHighlights()).isEqualTo("Shop Mall, 4.9 sao");
        assertThat(result.getDisclaimer()).contains("bản ghi đã lưu");
    }

    @Test
    void compare_shouldAskForProductWhenNameIsMissing() {
        MarketComparisonResponseDTO result = service.compare(null, "So sánh giá với Shopee");

        assertThat(result.isResolved()).isFalse();
        assertThat(result.getSummary()).contains("một mẫu");
        verify(productService, times(0)).searchProducts(anyString(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void compare_shouldSearchCatalogWhenMessageNamesAProduct() {
        ProductResponseDTO product = sampleProduct();
        when(productService.searchProducts(eq("nike dunk"), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(product));
        when(listingRepository.findByProduct_Id(1)).thenReturn(List.of());
        when(geminiService.chat(anyString())).thenThrow(new RuntimeException("gemini down"));

        MarketComparisonResponseDTO result = service.compare(null, "So sánh Nike Dunk với Shopee");

        assertThat(result.isResolved()).isTrue();
        assertThat(result.getProduct().getName()).isEqualTo("Nike Dunk Low");
        assertThat(result.getSummary()).contains("Nike Dunk Low");
        assertThat(offer(result, "TIKI").getUrl()).contains("tiki.vn");
        assertThat(result.getOffers()).allMatch(offer -> offer.getPrice() == null || offer.isOurs());
    }

    @Test
    void compare_shouldReuseCachedResult() {
        ProductResponseDTO product = sampleProduct();
        when(productService.fetchProductById(1)).thenReturn(product);
        when(listingRepository.findByProduct_Id(1)).thenReturn(List.of());
        when(geminiService.chat(anyString())).thenReturn("""
                {"summary": "Đã đối chiếu.", "verdict": "Xem link", "platforms": []}
                """);

        service.compare(1, "So sánh");
        service.compare(1, "So sánh lần nữa");

        verify(geminiService, times(1)).chat(anyString());
    }

    private ProductResponseDTO sampleProduct() {
        ProductResponseDTO product = new ProductResponseDTO();
        product.setId(1);
        product.setName("Nike Dunk Low");
        product.setDescription("Da thật");
        product.setPrice(2_500_000);
        product.setQuantity(4);
        return product;
    }

    private MarketplaceOfferDTO offer(MarketComparisonResponseDTO result, String platform) {
        return result.getOffers().stream()
                .filter(item -> platform.equals(item.getPlatform()))
                .findFirst()
                .orElseThrow();
    }
}
