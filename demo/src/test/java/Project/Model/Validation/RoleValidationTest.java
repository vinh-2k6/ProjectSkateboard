package Project.Model.Validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import Project.Model.Entity.Role;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class RoleValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validateRole_shouldPassWhenFieldsAreValid() {
        // Arrange
        Role role = new Role();
        role.setName("ADMIN");
        role.setDescription("Administrator");

        // Act
        var violations = validator.validate(role);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void validateRole_shouldReturnViolationsWhenFieldsAreBlank() {
        // Arrange
        Role role = new Role();
        role.setName("");
        role.setDescription(" ");

        // Act
        var violations = validator.validate(role);

        // Assert
        assertThat(violations).hasSize(2);

        assertThat(violations)
                .extracting(v -> v.getMessage())
                .containsExactlyInAnyOrder(
                        "name không được để trống",
                        "description không được để trống"
                );
    }
}
