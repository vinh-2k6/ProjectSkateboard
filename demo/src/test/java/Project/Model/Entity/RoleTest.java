
package Project.Model.Entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void createRole_shouldInitializeAllFields() {
        // Arrange
        List<User> users = new ArrayList<>();

        // Act
        Role role = new Role(
                1,
                "ADMIN",
                "Administrator role",
                users
        );

        // Assert
        assertThat(role.getId()).isEqualTo(1);
        assertThat(role.getName()).isEqualTo("ADMIN");
        assertThat(role.getDescription()).isEqualTo("Administrator role");
        assertThat(role.getUsers()).isSameAs(users);
    }

    @Test
    void setRole_shouldUpdateAllFields() {
        // Arrange
        Role role = new Role();

        List<User> users = new ArrayList<>();

        // Act
        role.setId(2);
        role.setName("USER");
        role.setDescription("Normal user role");
        role.setUsers(users);

        // Assert
        assertThat(role.getId()).isEqualTo(2);
        assertThat(role.getName()).isEqualTo("USER");
        assertThat(role.getDescription()).isEqualTo("Normal user role");
        assertThat(role.getUsers()).isSameAs(users);
    }

    @Test
    void noArgsConstructor_shouldInitializeFieldsAsNull() {
        // Act
        Role role = new Role();

        // Assert
        assertThat(role.getId()).isNull();
        assertThat(role.getName()).isNull();
        assertThat(role.getDescription()).isNull();
        assertThat(role.getUsers()).isNull();
    }
}
