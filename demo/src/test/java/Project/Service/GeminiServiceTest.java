
package Project.Service;

import Project.Model.DTO.Request.ProductSearchDTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.GenerateContentResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private GeminiService.ContentGenerator contentGenerator;

    @Mock
    private GenerateContentResponse response;

    private ObjectMapper objectMapper;

    private GeminiService geminiService;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        geminiService = new GeminiService(
                objectMapper,
                contentGenerator
        );
    }

    // Test 1: Chat trả về nội dung từ Gemini
    @Test
    void chat_shouldReturnResponseText() {

        // Arrange
        String message = "Xin chào";

        when(contentGenerator.generate(
                eq("gemini-3.7-flash"),
                eq(message),
                isNull()))
                .thenReturn(response);

        when(response.text())
                .thenReturn("Xin chào khách hàng!");

        // Act
        String result = geminiService.chat(message);

        // Assert
        assertThat(result)
                .isEqualTo("Xin chào khách hàng!");

        verify(contentGenerator).generate(
                "gemini-3.7-flash",
                message,
                null
        );
    }

    // Test 2: Parse JSON thành ProductSearchDTO thành công
    @Test
    void extractProductSearch_shouldReturnDTOWhenJsonIsValid() {

        // Arrange
        String message = "Tìm giày Nike dưới 2 triệu";

        List<String> categories =
                List.of("Running", "Basketball");

        List<String> brands =
                List.of("Nike", "Adidas");

        String json = """
                {
                    "keyword": "giày",
                    "brand": "Nike",
                    "minPrice": null,
                    "maxPrice": 2000000,
                    "category": null
                }
                """;

        when(contentGenerator.generate(
                eq("gemini-3.7-flash"),
                anyString(),
                any()))
                .thenReturn(response);

        when(response.text())
                .thenReturn(json);

        // Act
        ProductSearchDTO result =
                geminiService.extractProductSearch(
                        message,
                        categories,
                        brands
                );

        // Assert
        assertThat(result.getKeyword())
                .isEqualTo("giày");

        assertThat(result.getBrand())
                .isEqualTo("Nike");

        assertThat(result.getMinPrice())
                .isNull();

        assertThat(result.getMaxPrice())
                .isEqualTo(2000000);

        assertThat(result.getCategory())
                .isNull();
    }

    // Test 3: Tất cả điều kiện đều null
    @Test
    void extractProductSearch_shouldReturnNullCriteriaWhenJsonContainsNulls() {

        // Arrange
        String json = """
                {
                    "keyword": null,
                    "brand": null,
                    "minPrice": null,
                    "maxPrice": null,
                    "category": null
                }
                """;

        when(contentGenerator.generate(
                eq("gemini-3.7-flash"),
                anyString(),
                any()))
                .thenReturn(response);

        when(response.text())
                .thenReturn(json);

        // Act
        ProductSearchDTO result =
                geminiService.extractProductSearch(
                        "Xin chào",
                        List.of("Running"),
                        List.of("Nike")
                );

        // Assert
        assertThat(result.getKeyword()).isNull();
        assertThat(result.getBrand()).isNull();
        assertThat(result.getMinPrice()).isNull();
        assertThat(result.getMaxPrice()).isNull();
        assertThat(result.getCategory()).isNull();
    }

    // Test 4: JSON không hợp lệ
    @Test
    void extractProductSearch_shouldThrowRuntimeExceptionWhenJsonIsInvalid() {

        // Arrange
        when(contentGenerator.generate(
                eq("gemini-3.7-flash"),
                anyString(),
                any()))
                .thenReturn(response);

        when(response.text())
                .thenReturn("{ invalid json }");

        // Act & Assert
        assertThatThrownBy(() ->
                geminiService.extractProductSearch(
                        "Tìm giày Nike",
                        List.of("Running"),
                        List.of("Nike")
                ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(
                        "Không thể parse response từ Gemini"
                );
    }

    // Test 5: Kiểm tra prompt có chứa danh sách category và brand
    @Test
    void extractProductSearch_shouldIncludeCategoriesAndBrandsInPrompt() {

        // Arrange
        String message = "Tìm giày chạy bộ";

        List<String> categories =
                List.of("Running", "Basketball");

        List<String> brands =
                List.of("Nike", "Adidas");

        when(contentGenerator.generate(
                eq("gemini-3.7-flash"),
                anyString(),
                any()))
                .thenReturn(response);

        when(response.text())
                .thenReturn("""
                        {
                            "keyword": "giày",
                            "brand": null,
                            "minPrice": null,
                            "maxPrice": null,
                            "category": "Running"
                        }
                        """);

        // Act
        geminiService.extractProductSearch(
                message,
                categories,
                brands
        );

        // Assert
        ArgumentCaptor<String> promptCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(contentGenerator).generate(
                eq("gemini-3.7-flash"),
                promptCaptor.capture(),
                any()
        );

        String prompt = promptCaptor.getValue();

        assertThat(prompt)
                .contains("Running")
                .contains("Basketball")
                .contains("Nike")
                .contains("Adidas")
                .contains(message);
    }
}
