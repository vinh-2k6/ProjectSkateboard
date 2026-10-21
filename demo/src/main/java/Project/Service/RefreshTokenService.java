package Project.Service;

import org.springframework.stereotype.Service;

import Project.Exception.ResourceNotFoundException;
import Project.Model.Entity.RefreshToken;
import Project.Repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public void createRefreshToken(RefreshToken refreshToken) {
        this.refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken findRefreshTokenByToken(String token) {
        return this.refreshTokenRepository.findByToken(token)
                                        .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay token"));
    }

    public void deleteById(int id) {
		this.refreshTokenRepository.deleteById(id);
	}
}
