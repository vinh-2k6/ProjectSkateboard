package Project.Controller;

import org.springframework.web.bind.annotation.*;

import Project.Model.DTO.Request.ChatRequestDTO;
import Project.Model.DTO.Response.ChatResponseDTO;
import Project.Service.ChatService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor 
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    public ChatResponseDTO chat(String message) {
        return chatService.chat(message);
    }
}
