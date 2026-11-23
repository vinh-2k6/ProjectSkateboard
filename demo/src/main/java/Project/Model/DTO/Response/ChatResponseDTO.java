package Project.Model.DTO.Response;

import java.util.List;

import Project.Model.Enum.MessageType;
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
public class ChatResponseDTO {

    private MessageType type;

    private String message;

    private List<ProductResponseDTO> products;

    private Object data;

    public ChatResponseDTO(String message, List<ProductResponseDTO> products) {
        this.type = MessageType.TEXT;
        this.message = message;
        this.products = products;
    }

    public ChatResponseDTO(MessageType type, String message, List<ProductResponseDTO> products) {
        this.type = type;
        this.message = message;
        this.products = products;
    }

    // Getter hỗ trợ frontend đọc qua cả .reply và .message
    public String getReply() {
        return message;
    }
}
