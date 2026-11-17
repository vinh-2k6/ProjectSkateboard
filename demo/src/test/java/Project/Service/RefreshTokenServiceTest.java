package Project.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Project.Exception.ResourceNotFoundException;
import Project.Model.Entity.RefreshToken;
import Project.Repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    // 1. createRefreshToken
    @Test
    void createRefreshToken_shouldSaveSuccessfully() {

        RefreshToken refreshToken = new RefreshToken();

        refreshTokenService.createRefreshToken(refreshToken);

        verify(refreshTokenRepository).save(refreshToken);
    }

    // 2. findRefreshTokenByToken thành công
    @Test
    void findRefreshTokenByToken_shouldReturnTokenWhenFound() {

        String token = "refresh-token-123";

        RefreshToken refreshToken = new RefreshToken();

        when(refreshTokenRepository.findByToken(token))
                .thenReturn(Optional.of(refreshToken));

        RefreshToken result =
                refreshTokenService.findRefreshTokenByToken(token);

        assertNotNull(result);
        assertSame(refreshToken, result);

        verify(refreshTokenRepository).findByToken(token);
    }

    // 3. findRefreshTokenByToken không tìm thấy
    @Test
    void findRefreshTokenByToken_shouldThrowExceptionWhenNotFound() {

        String token = "invalid-token";

        when(refreshTokenRepository.findByToken(token))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> refreshTokenService.findRefreshTokenByToken(token)
        );

        verify(refreshTokenRepository).findByToken(token);
    }

    // 4. deleteById
    @Test
    void deleteById_shouldDeleteSuccessfully() {

        int id = 1;

        refreshTokenService.deleteById(id);

        verify(refreshTokenRepository).deleteById(id);
    }
}