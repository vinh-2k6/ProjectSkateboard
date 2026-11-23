package Project.Model.DTO.Response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutfitResponseDTO {
    private String outfitName;          // Tên set đồ (ví dụ: "Streetwear Skater Vibe")
    private String style;               // Phong cách (Streetwear, Sporty, Clean Fit, Retro...)
    private String occasion;            // Dịp sử dụng (Dạo phố, Đi học, Đi tập, Trượt ván...)
    private ProductResponseDTO shoe;    // Đôi giày được chọn phối cùng
    private List<OutfitItemDTO> items;   // Danh sách món đồ (Áo, Quần, Phụ kiện)
    private String stylingTips;          // Lời khuyên của Stylist chuyên nghiệp
    private String imageUrl;             // Link ảnh AI Lookbook vẽ cả set đồ người mẫu mặc
}
