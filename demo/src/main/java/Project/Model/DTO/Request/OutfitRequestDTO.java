package Project.Model.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutfitRequestDTO {
    private Integer productId;  // ID đôi giày muốn phối cùng (nếu có)
    private String style;        // Phong cách mong muốn (Streetwear, Sporty, Casual, Y2K...)
    private String occasion;     // Dịp sử dụng (Đi chơi, Đi học, Đi làm, Trượt ván...)
    private String message;      // Ghi chú hoặc câu hỏi thêm của khách hàng
}
