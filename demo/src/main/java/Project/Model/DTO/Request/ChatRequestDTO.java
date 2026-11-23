package Project.Model.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDTO {

    private String message;
    private Integer productId;
    private String style;       // "Streetwear", "Sporty", "Casual", "Retro"...
    private String occasion;    // "Đi học", "Đi chơi", "Trượt ván", "Dạo phố"...
    private String type;        // "TEXT", "PRODUCT_DETAIL", "OUTFIT_RECOMMENDATION"

    public ChatRequestDTO(String message, Integer productId) {
        this.message = message;
        this.productId = productId;
    }
}
