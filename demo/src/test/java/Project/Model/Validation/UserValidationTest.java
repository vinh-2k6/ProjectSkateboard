package Project.Model.Validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import Project.Model.Entity.User;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class UserValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validateUser_shouldPassWhenNameAndEmailAreValid() {
        // Arrange
        User user = new User();
        user.setName("Nguyen Van A");
        user.setEmail("user@gmail.com");

        // Act
        var violations = validator.validate(user);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void validateUser_shouldReturnViolationsWhenNameAndEmailAreBlank() {
        // Arrange
        User user = new User();
        user.setName("");
        user.setEmail(" ");

        // Act
        var violations = validator.validate(user);

        // Assert
        assertThat(violations).hasSize(2);

        assertThat(violations)
                .extracting(v -> v.getMessage())
                .containsExactlyInAnyOrder(
                        "name không được để trống",
                        "email không được để trống"
                );
    }

    @Test
    void validateUser_shouldAllowNullOptionalFields() {
        // Arrange
        User user = new User();
        user.setName("Nguyen Van A");
        user.setEmail("user@gmail.com");

        user.setPassword(null);
        user.setPhone(null);
        user.setAddress(null);
        user.setGoogleId(null);

        // Act
        var violations = validator.validate(user);

        // Assert
        assertThat(violations).isEmpty();
    }
}
