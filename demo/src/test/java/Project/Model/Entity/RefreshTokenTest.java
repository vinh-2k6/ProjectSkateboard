
package Project.Model.Entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class RefreshTokenTest {

    @Test
    void createRefreshToken_shouldInitializeAllFields() {
        // Arrange
        User user = new User();

        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(604800);

        // Act
        RefreshToken token = new RefreshToken(
                1,
                "refresh-token-123",
                user,
                createdAt,
                expiresAt
        );

        // Assert
        assertThat(token.getId()).isEqualTo(1);
        assertThat(token.getToken()).isEqualTo("refresh-token-123");
        assertThat(token.getUser()).isSameAs(user);
        assertThat(token.getCreatedAt()).isEqualTo(createdAt);
        assertThat(token.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void setRefreshToken_shouldUpdateAllFields() {
        // Arrange
        RefreshToken token = new RefreshToken();

        User user = new User();

        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(604800);

        // Act
        token.setId(2);
        token.setToken("new-refresh-token");
        token.setUser(user);
        token.setCreatedAt(createdAt);
        token.setExpiresAt(expiresAt);

        // Assert
        assertThat(token.getId()).isEqualTo(2);
        assertThat(token.getToken()).isEqualTo("new-refresh-token");
        assertThat(token.getUser()).isSameAs(user);
        assertThat(token.getCreatedAt()).isEqualTo(createdAt);
        assertThat(token.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void noArgsConstructor_shouldInitializeFieldsAsNull() {
        // Act
        RefreshToken token = new RefreshToken();

        // Assert
        assertThat(token.getId()).isNull();
        assertThat(token.getToken()).isNull();
        assertThat(token.getUser()).isNull();
        assertThat(token.getCreatedAt()).isNull();
        assertThat(token.getExpiresAt()).isNull();
    }
}
