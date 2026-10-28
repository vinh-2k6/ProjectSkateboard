package Project.Model.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequestDTO {

    @NotBlank(message = "Google credential không được để trống")
    private String credential;
}