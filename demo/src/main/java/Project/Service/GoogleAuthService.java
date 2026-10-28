package Project.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import Project.Config.JWTService;
import Project.Model.DTO.Response.LoginResponseDTO;
import Project.Model.Entity.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserService userService;
    private final JWTService jwtService;

    public LoginResponseDTO login(String credential) {
        final GoogleIdToken googleIdToken;

        try {
            googleIdToken = googleIdTokenVerifier.verify(credential);
        } catch (GeneralSecurityException | IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Không thể xác minh Google ID Token lúc này."
            );
        }

        if (googleIdToken == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Google ID Token không hợp lệ hoặc đã hết hạn."
            );
        }

        GoogleIdToken.Payload payload = googleIdToken.getPayload();

        String googleId = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        boolean emailVerified =
                Boolean.TRUE.equals(payload.getEmailVerified());

        if (googleId == null || email == null || !emailVerified) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Tài khoản Google chưa có email hợp lệ được xác minh."
            );
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String hostedDomain = payload.getHostedDomain();

        boolean trustedGoogleEmail =
                normalizedEmail.endsWith("@gmail.com")
                || (hostedDomain != null && !hostedDomain.isBlank());

        User user = userService.findOrCreateGoogleUser(
                googleId,
                normalizedEmail,
                name
        );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().getName()
                        ))
                );

        String accessToken = jwtService.createAccessToken(
                authentication,
                user.getId()
        );

        String refreshToken = jwtService.createRefreshToken(user);

        return new LoginResponseDTO(
                accessToken,
                refreshToken,
                "Bearer",
                new LoginResponseDTO.UserLogin(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().getName()
                )
        );
    }
}