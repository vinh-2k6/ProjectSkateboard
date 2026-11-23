package Project.Model.DTO.Response;

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
public class OutfitItemDTO {
    private String category;   // "Áo (Top)", "Quần (Bottom)", "Phụ kiện (Accessories)"
    private String name;       // Tên món đồ gợi ý (ví dụ: "Áo hoodie oversize nỉ bông")
    private String color;      // Màu sắc gợi ý (ví dụ: "Xám tiêu / Đen")
    private String note;       // Lưu ý / lý do chọn món này
}
