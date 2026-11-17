
package Project.Service;

import Project.Config.JWTService;
import Project.Model.DTO.Response.LoginResponseDTO;
import Project.Model.Entity.Role;
import Project.Model.Entity.User;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {

    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @Mock
    private UserService userService;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private GoogleAuthService googleAuthService;

    @Mock
    private GoogleIdToken googleIdToken;

    @Mock
    private GoogleIdToken.Payload payload;

    private User user;

    @BeforeEach
    void setUp() {

        Role role = new Role(
                2,
                "USER",
                "Normal user",
                null
        );

        user = new User();
        user.setId(1);
        user.setName("Nguyen Van A");
        user.setEmail("test@gmail.com");
        user.setRole(role);
        user.setGoogleId("google-123");
    }

    // Test 1: Đăng nhập Google thành công
    @Test
    void login_shouldReturnLoginResponseWhenTokenIsValid()
            throws Exception {

        // Arrange
        String credential = "valid-google-token";

        when(googleIdTokenVerifier.verify(credential))
                .thenReturn(googleIdToken);

        when(googleIdToken.getPayload())
                .thenReturn(payload);

        when(payload.getSubject())
                .thenReturn("google-123");

        when(payload.getEmail())
                .thenReturn("test@gmail.com");

        when(payload.get("name"))
                .thenReturn("Nguyen Van A");

        when(payload.getEmailVerified())
                .thenReturn(true);

        when(payload.getHostedDomain())
                .thenReturn(null);

        when(userService.findOrCreateGoogleUser(
                "google-123",
                "test@gmail.com",
                "Nguyen Van A"))
                .thenReturn(user);

        when(jwtService.createAccessToken(
                any(Authentication.class), eq(1)))
                .thenReturn("access-token");

        when(jwtService.createRefreshToken(user))
                .thenReturn("refresh-token");

        // Act
        LoginResponseDTO result =
                googleAuthService.login(credential);

        // Assert
        assertThat(result.getAccessToken())
                .isEqualTo("access-token");

        assertThat(result.getRefreshToken())
                .isEqualTo("refresh-token");

        assertThat(result.getTokenType())
                .isEqualTo("Bearer");

        assertThat(result.getUser().getId())
                .isEqualTo(1);

        assertThat(result.getUser().getUsername())
                .isEqualTo("test@gmail.com");

        assertThat(result.getUser().getRole())
                .isEqualTo("USER");

        verify(userService).findOrCreateGoogleUser(
                "google-123",
                "test@gmail.com",
                "Nguyen Van A");

        verify(jwtService).createAccessToken(
                any(Authentication.class), eq(1));

        verify(jwtService).createRefreshToken(user);
    }

    // Test 2: Google trả về null token
    @Test
    void login_shouldThrowUnauthorizedWhenTokenIsNull()
            throws Exception {

        // Arrange
        when(googleIdTokenVerifier.verify("invalid-token"))
                .thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() ->
                googleAuthService.login("invalid-token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);
                });

        verifyNoInteractions(userService, jwtService);
    }

    // Test 3: Email chưa được xác minh
    @Test
    void login_shouldThrowUnauthorizedWhenEmailNotVerified()
            throws Exception {

        // Arrange
        when(googleIdTokenVerifier.verify("token"))
                .thenReturn(googleIdToken);

        when(googleIdToken.getPayload())
                .thenReturn(payload);

        when(payload.getSubject())
                .thenReturn("google-123");

        when(payload.getEmail())
                .thenReturn("test@gmail.com");

        when(payload.getEmailVerified())
                .thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() ->
                googleAuthService.login("token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);
                });

        verifyNoInteractions(userService, jwtService);
    }

    // Test 4: Google ID bị null
    @Test
    void login_shouldThrowUnauthorizedWhenGoogleIdIsNull()
            throws Exception {

        // Arrange
        when(googleIdTokenVerifier.verify("token"))
                .thenReturn(googleIdToken);

        when(googleIdToken.getPayload())
                .thenReturn(payload);

        when(payload.getSubject())
                .thenReturn(null);

        when(payload.getEmail())
                .thenReturn("test@gmail.com");

        when(payload.getEmailVerified())
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() ->
                googleAuthService.login("token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);
                });

        verifyNoInteractions(userService, jwtService);
    }

    // Test 5: Email bị null
    @Test
    void login_shouldThrowUnauthorizedWhenEmailIsNull()
            throws Exception {

        // Arrange
        when(googleIdTokenVerifier.verify("token"))
                .thenReturn(googleIdToken);

        when(googleIdToken.getPayload())
                .thenReturn(payload);

        when(payload.getSubject())
                .thenReturn("google-123");

        when(payload.getEmail())
                .thenReturn(null);

        when(payload.getEmailVerified())
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() ->
                googleAuthService.login("token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);
                });

        verifyNoInteractions(userService, jwtService);
    }

    // Test 6: Google verifier gặp IOException
    @Test
    void login_shouldThrowServiceUnavailableWhenIOExceptionOccurs()
            throws Exception {

        // Arrange
        when(googleIdTokenVerifier.verify("token"))
                .thenThrow(new IOException("Connection error"));

        // Act & Assert
        assertThatThrownBy(() ->
                googleAuthService.login("token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });

        verifyNoInteractions(userService, jwtService);
    }

    // Test 7: Google verifier gặp GeneralSecurityException
    @Test
    void login_shouldThrowServiceUnavailableWhenSecurityExceptionOccurs()
            throws Exception {

        // Arrange
        when(googleIdTokenVerifier.verify("token"))
                .thenThrow(new GeneralSecurityException("Security error"));

        // Act & Assert
        assertThatThrownBy(() ->
                googleAuthService.login("token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });

        verifyNoInteractions(userService, jwtService);
    }

    // Test 8: Chuẩn hóa email về chữ thường
    @Test
    void login_shouldNormalizeEmailBeforeFindingUser()
            throws Exception {

        // Arrange
        when(googleIdTokenVerifier.verify("token"))
                .thenReturn(googleIdToken);

        when(googleIdToken.getPayload())
                .thenReturn(payload);

        when(payload.getSubject())
                .thenReturn("google-123");

        when(payload.getEmail())
                .thenReturn("  TEST@GMAIL.COM  ");

        when(payload.getEmailVerified())
                .thenReturn(true);

        when(payload.getHostedDomain())
                .thenReturn(null);

        when(payload.get("name"))
                .thenReturn("Nguyen Van A");

        when(userService.findOrCreateGoogleUser(
                "google-123",
                "test@gmail.com",
                "Nguyen Van A"))
                .thenReturn(user);

        when(jwtService.createAccessToken(
                any(Authentication.class), eq(1)))
                .thenReturn("access-token");

        when(jwtService.createRefreshToken(user))
                .thenReturn("refresh-token");

        // Act
        LoginResponseDTO result =
                googleAuthService.login("token");

        // Assert
        assertThat(result.getUser().getUsername())
                .isEqualTo("test@gmail.com");

        verify(userService).findOrCreateGoogleUser(
                "google-123",
                "test@gmail.com",
                "Nguyen Van A");
    }
}
