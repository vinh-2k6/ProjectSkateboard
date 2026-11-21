package Project.Service;

import Project.Model.DTO.Request.ChatRequestDTO;
import Project.Model.DTO.Request.ProductSearchDTO;
import Project.Model.DTO.Response.ChatResponseDTO;
import Project.Model.DTO.Response.ProductResponseDTO;
import Project.Model.Enum.MessageType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ChatService {

    private final ProductService productService;
    private final GeminiService geminiService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public ChatResponseDTO chat(String message) {
        return chat(new ChatRequestDTO(message, null));
    }

    public ChatResponseDTO chat(ChatRequestDTO request) {
        String message = request.getMessage() != null ? request.getMessage().trim() : "";
        Integer productId = request.getProductId();

        // 1. Nếu khách hàng đang xem hoặc hỏi về một sản phẩm cụ thể (có productId)
        if (productId != null) {
            return consultProductDetail(productId, message);
        }

        // 2. Luồng chung khi không có productId: Phân tích tìm kiếm hoặc chat thông thường
        return handleGeneralChat(message);
    }

    /**
     * AI tư vấn chi tiết về một sản phẩm cụ thể dựa trên dữ liệu database
     * Chỉ gọi Gemini đúng 1 lần duy nhất với context sản phẩm đầy đủ để tối đa hóa tốc độ phản hồi.
     */
    public ChatResponseDTO consultProductDetail(Integer productId, String userMessage) {
        try {
            ProductResponseDTO product = productService.fetchProductById(productId);

            String brandName = product.getBrand() != null ? product.getBrand().getName() : "ProjectSkateBoard";
            String categoryName = product.getCategory() != null ? product.getCategory().getName() : "Không xác định";
            String stockStatus = (product.getQuantity() != null && product.getQuantity() > 0)
                    ? "Còn hàng (" + product.getQuantity() + " sản phẩm)"
                    : "Tạm hết hàng";
            String formattedPrice = product.getPrice() != null 
                    ? String.format("%,d VNĐ", product.getPrice()) 
                    : "Liên hệ";

            String prompt = """
                    Bạn là chuyên viên tư vấn bán hàng chuyên nghiệp, am hiểu và nhiệt tình của cửa hàng ProjectSkateBoard.
                    Khách hàng đang xem và hỏi về sản phẩm cụ thể sau:
                    - Tên sản phẩm: %s
                    - Thương hiệu: %s
                    - Danh mục: %s
                    - Giá bán: %s
                    - Tình trạng: %s
                    - Mô tả chi tiết: %s

                    Câu hỏi của khách hàng:
                    "%s"

                    QUY TẮC TƯ VẤN:
                    1. Trả lời trực tiếp, thân thiện, xưng hô "mình" - "bạn" (hoặc "ProjectSkateBoard").
                    2. Đánh giá ưu điểm, công dụng, độ phù hợp (ví dụ: người mới bắt đầu hay dân chuyên, phong cách đi kèm, độ bền) dựa trên thông tin sản phẩm trên.
                    3. Tuyệt đối KHÔNG tự bịa đặt thông số kỹ thuật hoặc giá bán khác với thông tin đã cung cấp.
                    4. Nếu khách hàng hỏi những chi tiết không có trong mô tả, hãy trung thực trả lời và gợi ý khách nhắn tin fanpage hoặc liên hệ hotline để được nhân viên hỗ trợ trực tiếp.
                    5. Trả lời bằng tiếng Việt, ngắn gọn, súc tích và dễ đọc.
                    """.formatted(
                    product.getName(),
                    brandName,
                    categoryName,
                    formattedPrice,
                    stockStatus,
                    product.getDescription() != null ? product.getDescription() : "Chưa có mô tả chi tiết",
                    (userMessage != null && !userMessage.isBlank()) ? userMessage : "Tư vấn chi tiết về sản phẩm này giúp mình nhé."
            );

            String reply = geminiService.chat(prompt);

            return ChatResponseDTO.builder()
                    .type(MessageType.PRODUCT_DETAIL)
                    .message(reply)
                    .products(List.of(product))
                    .data(product)
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi tư vấn chi tiết sản phẩm id={}: {}", productId, e.getMessage());
            // Fallback sang chat chung nếu không tìm thấy productId
            return handleGeneralChat(userMessage);
        }
    }

    /**
     * Luồng chat chung: Tìm kiếm sản phẩm hoặc hỏi đáp tổng quát
     */
    private ChatResponseDTO handleGeneralChat(String message) {
        if (message == null || message.isBlank()) {
            return new ChatResponseDTO(
                    MessageType.TEXT,
                    "Xin chào! Mình là tư vấn viên ProjectSkateBoard. Bạn cần tìm mẫu giày hay ván trượt nào?",
                    List.of()
            );
        }

        // 1. Lấy danh sách category và brand hiện tại từ database
        List<String> categories = categoryService.getCategoryNames();
        List<String> brands = brandService.getBrandNames();

        // 2. Gemini phân tích câu hỏi thành điều kiện tìm kiếm (có fallback an toàn)
        ProductSearchDTO criteria;
        try {
            criteria = geminiService.extractProductSearch(
                    message,
                    categories,
                    brands
            );
        } catch (Exception e) {
            log.warn("Không thể trích xuất tiêu chí tìm kiếm từ Gemini: {}", e.getMessage());
            criteria = new ProductSearchDTO(message, null, null, null, null);
        }

        boolean hasCriteria = criteria.getKeyword() != null ||
                criteria.getBrand() != null ||
                criteria.getMinPrice() != null ||
                criteria.getMaxPrice() != null ||
                criteria.getCategory() != null;

        // Không có điều kiện tìm kiếm -> chỉ chat thông thường
        if (!hasCriteria) {
            String reply = geminiService.chat("""
                    Bạn là chatbot của cửa hàng ProjectSkateBoard.
                    Hãy trả lời ngắn gọn, thân thiện câu hỏi của khách hàng bằng tiếng Việt.
                    
                    Khách hàng:
                    %s
                    """.formatted(message));

            return new ChatResponseDTO(MessageType.TEXT, reply, List.of());
        }

        // 3. Có điều kiện -> tìm sản phẩm trong database
        List<ProductResponseDTO> products = productService.searchProducts(
                criteria.getKeyword(),
                criteria.getBrand(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getCategory()
        );

        // 4. Chuyển danh sách sản phẩm thành context cho Gemini
        StringBuilder productInfo = new StringBuilder();
        for (ProductResponseDTO product : products) {
            productInfo.append("- ")
                    .append(product.getName())
                    .append(" - ")
                    .append(product.getPrice())
                    .append(" VND\n");
        }

        // 5. Gemini tạo câu trả lời tự nhiên
        String prompt = """
                Bạn là chatbot của cửa hàng ProjectSkateBoard.

                Khách hàng hỏi:
                %s

                Các sản phẩm tìm được trong database:
                %s

                Hãy tư vấn cho khách hàng dựa trên danh sách sản phẩm trên.
                Không được tự tạo ra sản phẩm không có trong danh sách.
                Nếu danh sách sản phẩm trống, hãy nói rằng hiện tại không tìm thấy sản phẩm phù hợp.
                Trả lời bằng tiếng Việt.
                """.formatted(message, productInfo);

        String reply = geminiService.chat(prompt);

        return new ChatResponseDTO(MessageType.TEXT, reply, products);
    }
}
