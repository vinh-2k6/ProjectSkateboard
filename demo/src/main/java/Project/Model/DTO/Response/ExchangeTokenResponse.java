package Project.Model.DTO.Response;

import Project.Model.DTO.Response.LoginResponseDTO.UserLogin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeTokenResponse {

	private String accessToken;

	private String refreshToken;

	private String tokenType;

	private UserLogin user;
}
