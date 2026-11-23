
package Project.Service;

import Project.Model.DTO.Request.ProductSearchDTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final String DEFAULT_MODEL = "gemini-3.5-flash";
    private static final String FALLBACK_MODEL = "gemini-flash-lite-latest";

    private final ObjectMapper objectMapper;
    private final ContentGenerator contentGenerator;
    private final String modelName;

    // Constructor sử dụng khi chạy ứng dụng thật
    @Autowired
    public GeminiService(
            Client client,
            ObjectMapper objectMapper,
            @org.springframework.beans.factory.annotation.Value("${gemini.model:gemini-3.5-flash}") String modelName) {

        this.objectMapper = objectMapper;
        this.modelName = (modelName != null && !modelName.isBlank()) ? modelName : DEFAULT_MODEL;

        this.contentGenerator = (model, prompt, config) ->
                client.models.generateContent(
                        model,
                        prompt,
                        config
                );
    }

    // Constructor sử dụng trong Unit Test
    GeminiService(
            ObjectMapper objectMapper,
            ContentGenerator contentGenerator) {

        this.objectMapper = objectMapper;
        this.contentGenerator = contentGenerator;
        this.modelName = DEFAULT_MODEL;
    }

    @FunctionalInterface
    interface ContentGenerator {

        GenerateContentResponse generate(
                String model,
                String prompt,
                GenerateContentConfig config
        );
    }

    public String chat(String message) {
        GenerateContentResponse response;
        try {
            response = contentGenerator.generate(
                    this.modelName,
                    message,
                    null
            );
        } catch (Exception e) {
            response = contentGenerator.generate(
                    FALLBACK_MODEL,
                    message,
                    null
            );
        }

        return response.text();
    }

    public ProductSearchDTO extractProductSearch(
            String message,
            List<String> categories,
            List<String> brands) {

        String prompt = """
                Bạn là hệ thống phân tích yêu cầu tìm kiếm sản phẩm
                của cửa hàng giày ProjectSkateBoard.

                Các category hiện có trong database:
                %s

                Các brand hiện có trong database:
                %s

                Hãy phân tích câu hỏi của khách hàng và trả về
                ProductSearchDTO.

                QUY TẮC:

                - category phải là một giá trị trong danh sách category ở trên.
                - brand phải là một giá trị trong danh sách brand ở trên.
                - Nếu khách hàng dùng cách gọi khác,
                  hãy ánh xạ về giá trị tương ứng trong database.
                - Không tự tạo category hoặc brand mới.
                - Nếu không xác định được thì trả về null.

                Ví dụ:
                "giày chạy bộ" -> category tương ứng với Running
                nếu Running tồn tại trong danh sách.

                "giày bóng rổ" -> category tương ứng với Basketball
                nếu Basketball tồn tại trong danh sách.

                Về giá:
                - "dưới 2 triệu" -> maxPrice = 2000000
                - "trên 2 triệu" -> minPrice = 2000000
                - "từ 1 triệu đến 2 triệu"
                -> minPrice = 1000000
                -> maxPrice = 2000000

                Câu hỏi:
                %s
                """.formatted(categories, brands, message);

        Schema nullableString = Schema.builder()
                .anyOf(
                        Schema.builder()
                                .type(Type.Known.STRING)
                                .build(),

                        Schema.builder()
                                .type(Type.Known.NULL)
                                .build()
                )
                .build();

        Schema nullableInteger = Schema.builder()
                .anyOf(
                        Schema.builder()
                                .type(Type.Known.INTEGER)
                                .build(),

                        Schema.builder()
                                .type(Type.Known.NULL)
                                .build()
                )
                .build();

        Schema productSearchSchema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(
                        Map.of(
                                "keyword", nullableString,
                                "brand", nullableString,
                                "minPrice", nullableInteger,
                                "maxPrice", nullableInteger,
                                "category", nullableString
                        )
                )
                .build();

        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseMimeType("application/json")
                        .responseSchema(productSearchSchema)
                        .build();

        GenerateContentResponse response;
        try {
            response = contentGenerator.generate(
                    this.modelName,
                    prompt,
                    config
            );
        } catch (Exception e) {
            response = contentGenerator.generate(
                    FALLBACK_MODEL,
                    prompt,
                    config
            );
        }

        try {

            return objectMapper.readValue(
                    response.text(),
                    ProductSearchDTO.class
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Không thể parse response từ Gemini: "
                            + response.text(),
                    e
            );
        }
    }
}
