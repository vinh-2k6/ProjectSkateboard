
package Project.Service;

import Project.Model.DTO.Request.ChatRequestDTO;
import Project.Model.DTO.Request.ProductSearchDTO;
import Project.Model.DTO.Response.ChatResponseDTO;
import Project.Model.DTO.Response.MarketComparisonResponseDTO;
import Project.Model.DTO.Response.ProductResponseDTO;
import Project.Model.Enum.MessageType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private GeminiService geminiService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private BrandService brandService;

    @Mock
    private OutfitStylistService outfitStylistService;

    @Mock
    private MarketComparisonService marketComparisonService;

    @InjectMocks
    private ChatService chatService;

    private List<String> categories;
    private List<String> brands;

    @BeforeEach
    void setUp() {
        categories = List.of("Running", "Basketball");
        brands = List.of("Nike", "Adidas");
    }

    // Test 1: Chat thông thường, không có điều kiện tìm kiếm
    @Test
    void chat_shouldReturnNormalReplyWhenNoSearchCriteria() {

        // Arrange
        String message = "Xin chào shop";

        ProductSearchDTO criteria =
                new ProductSearchDTO(null, null, null, null, null);

        when(categoryService.getCategoryNames())
                .thenReturn(categories);

        when(brandService.getBrandNames())
                .thenReturn(brands);

        when(geminiService.extractProductSearch(
                message, categories, brands))
                .thenReturn(criteria);

        when(geminiService.chat(anyString()))
                .thenReturn("Xin chào! Shop có thể giúp gì cho bạn?");

        // Act
        ChatResponseDTO result = chatService.chat(message);

        // Assert
        assertThat(result.getMessage())
                .isEqualTo("Xin chào! Shop có thể giúp gì cho bạn?");

        assertThat(result.getProducts()).isEmpty();

        verify(geminiService).chat(anyString());

        verify(productService, never())
                .searchProducts(any(), any(), any(), any(), any());
    }

    // Test 2: Tìm kiếm sản phẩm thành công
    @Test
    void chat_shouldReturnProductsWhenSearchCriteriaExists() {

        // Arrange
        String message = "Tìm giày Nike dưới 2 triệu";

        ProductSearchDTO criteria =
                new ProductSearchDTO(
                        "giày",
                        "Nike",
                        null,
                        2000000,
                        null
                );

        ProductResponseDTO product =
                mock(ProductResponseDTO.class);

        List<ProductResponseDTO> products = List.of(product);

        when(categoryService.getCategoryNames())
                .thenReturn(categories);

        when(brandService.getBrandNames())
                .thenReturn(brands);

        when(geminiService.extractProductSearch(
                message, categories, brands))
                .thenReturn(criteria);

        when(productService.searchProducts(
                "giày", "Nike", null, 2000000, null))
                .thenReturn(products);

        when(geminiService.chat(anyString()))
                .thenReturn("Shop tìm được một sản phẩm phù hợp.");

        // Act
        ChatResponseDTO result = chatService.chat(message);

        // Assert
        assertThat(result.getMessage())
                .isEqualTo("Shop tìm được một sản phẩm phù hợp.");

        assertThat(result.getProducts())
                .hasSize(1)
                .containsExactly(product);

        verify(productService).searchProducts(
                "giày", "Nike", null, 2000000, null);

        verify(geminiService).chat(anyString());
    }

    // Test 3: Không tìm thấy sản phẩm
    @Test
    void chat_shouldReturnEmptyProductsWhenNoProductFound() {

        // Arrange
        String message = "Tìm giày Nike giá 100 triệu";

        ProductSearchDTO criteria =
                new ProductSearchDTO(
                        "giày",
                        "Nike",
                        100000000,
                        null,
                        null
                );

        when(categoryService.getCategoryNames())
                .thenReturn(categories);

        when(brandService.getBrandNames())
                .thenReturn(brands);

        when(geminiService.extractProductSearch(
                message, categories, brands))
                .thenReturn(criteria);

        when(productService.searchProducts(
                "giày", "Nike", 100000000, null, null))
                .thenReturn(List.of());

        when(geminiService.chat(anyString()))
                .thenReturn("Hiện tại shop không tìm thấy sản phẩm phù hợp.");

        // Act
        ChatResponseDTO result = chatService.chat(message);

        // Assert
        assertThat(result.getMessage())
                .contains("không tìm thấy");

        assertThat(result.getProducts()).isEmpty();

        verify(productService).searchProducts(
                "giày", "Nike", 100000000, null, null);
    }

    // Test 4: Chỉ có keyword vẫn phải tìm kiếm
    @Test
    void chat_shouldSearchWhenOnlyKeywordExists() {

        // Arrange
        String message = "Tìm giày thể thao";

        ProductSearchDTO criteria =
                new ProductSearchDTO(
                        "giày thể thao",
                        null,
                        null,
                        null,
                        null
                );

        when(categoryService.getCategoryNames())
                .thenReturn(categories);

        when(brandService.getBrandNames())
                .thenReturn(brands);

        when(geminiService.extractProductSearch(
                message, categories, brands))
                .thenReturn(criteria);

        when(productService.searchProducts(
                "giày thể thao", null, null, null, null))
                .thenReturn(List.of());

        when(geminiService.chat(anyString()))
                .thenReturn("Không tìm thấy sản phẩm.");

        // Act
        ChatResponseDTO result = chatService.chat(message);

        // Assert
        assertThat(result.getProducts()).isEmpty();

        verify(productService).searchProducts(
                "giày thể thao", null, null, null, null);
    }

    // Test 5: Kiểm tra context gửi cho Gemini
    @Test
    void chat_shouldIncludeProductInformationInPrompt() {

        // Arrange
        String message = "Tìm giày Nike";

        ProductSearchDTO criteria =
                new ProductSearchDTO(
                        "giày", "Nike", null, null, null
                );

        ProductResponseDTO product =
                mock(ProductResponseDTO.class);

        when(product.getName()).thenReturn("Nike Air Max");
        when(product.getPrice()).thenReturn(1500000);

        when(categoryService.getCategoryNames())
                .thenReturn(categories);

        when(brandService.getBrandNames())
                .thenReturn(brands);

        when(geminiService.extractProductSearch(
                message, categories, brands))
                .thenReturn(criteria);

        when(productService.searchProducts(
                "giày", "Nike", null, null, null))
                .thenReturn(List.of(product));

        when(geminiService.chat(anyString()))
                .thenReturn("Đây là sản phẩm phù hợp.");

        // Act
        chatService.chat(message);

        // Assert
        ArgumentCaptor<String> promptCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(geminiService).chat(promptCaptor.capture());

        String prompt = promptCaptor.getValue();

        assertThat(prompt)
                .contains("Nike Air Max")
                .contains("1500000")
                .contains(message);
    }

    @Test
    void chat_shouldReturnMarketplaceComparison() {
        ProductResponseDTO product = new ProductResponseDTO();
        product.setId(5);
        product.setName("Nike Dunk Low");

        MarketComparisonResponseDTO comparison = MarketComparisonResponseDTO.builder()
                .resolved(true)
                .summary("Giá shop đang cạnh tranh với Shopee và Tiki.")
                .product(product)
                .offers(List.of())
                .build();

        when(marketComparisonService.compare(5, "So sánh giá với Shopee"))
                .thenReturn(comparison);

        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage("So sánh giá với Shopee");
        request.setProductId(5);
        request.setType("MARKETPLACE_COMPARISON");

        ChatResponseDTO result = chatService.chat(request);

        assertThat(result.getType()).isEqualTo(MessageType.MARKETPLACE_COMPARISON);
        assertThat(result.getMessage()).contains("cạnh tranh");
        assertThat(result.getData()).isSameAs(comparison);
        assertThat(result.getProducts()).containsExactly(product);
        verify(geminiService, never()).extractProductSearch(any(), any(), any());
        verify(outfitStylistService, never()).generateOutfit(any(), any(), any(), any());
    }
}
