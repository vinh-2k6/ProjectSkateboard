package Project.Model.Validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import Project.Model.Entity.RefreshToken;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class RefreshTokenValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validateRefreshToken_shouldPassWhenTokenIsValid() {
        // Arrange
        RefreshToken token = new RefreshToken();
        token.setToken("refresh-token-123");

        // Act
        var violations = validator.validate(token);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void validateRefreshToken_shouldReturnViolationWhenTokenIsBlank() {
        // Arrange
        RefreshToken token = new RefreshToken();
        token.setToken("");

        // Act
        var violations = validator.validate(token);

        // Assert
        assertThat(violations).hasSize(1);

        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("token không được để trống");
    }
}
