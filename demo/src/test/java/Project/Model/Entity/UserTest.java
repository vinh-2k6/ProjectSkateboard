
package Project.Model.Entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void createUser_shouldInitializeAllFields() {
        // Arrange
        Role role = new Role();
        Cart cart = new Cart();

        List<Order> orders = new ArrayList<>();
        List<Comment> comments = new ArrayList<>();
        List<RefreshToken> refreshTokens = new ArrayList<>();

        // Act
        User user = new User(
                1,
                "Nguyen Van A",
                "user@gmail.com",
                "password123",
                "0123456789",
                "Hanoi",
                "google-id-123",
                role,
                orders,
                comments,
                refreshTokens,
                cart
        );

        // Assert
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getName()).isEqualTo("Nguyen Van A");
        assertThat(user.getEmail()).isEqualTo("user@gmail.com");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getPhone()).isEqualTo("0123456789");
        assertThat(user.getAddress()).isEqualTo("Hanoi");
        assertThat(user.getGoogleId()).isEqualTo("google-id-123");

        assertThat(user.getRole()).isSameAs(role);
        assertThat(user.getOrders()).isSameAs(orders);
        assertThat(user.getComments()).isSameAs(comments);
        assertThat(user.getRefreshTokens()).isSameAs(refreshTokens);
        assertThat(user.getCart()).isSameAs(cart);
    }

    @Test
    void setUser_shouldUpdateAllFields() {
        // Arrange
        User user = new User();

        Role role = new Role();
        Cart cart = new Cart();

        List<Order> orders = new ArrayList<>();
        List<Comment> comments = new ArrayList<>();
        List<RefreshToken> refreshTokens = new ArrayList<>();

        // Act
        user.setId(2);
        user.setName("Nguyen Van B");
        user.setEmail("user2@gmail.com");
        user.setPassword("password456");
        user.setPhone("0987654321");
        user.setAddress("Hanoi");
        user.setGoogleId("google-id-456");

        user.setRole(role);
        user.setOrders(orders);
        user.setComments(comments);
        user.setRefreshTokens(refreshTokens);
        user.setCart(cart);

        // Assert
        assertThat(user.getId()).isEqualTo(2);
        assertThat(user.getName()).isEqualTo("Nguyen Van B");
        assertThat(user.getEmail()).isEqualTo("user2@gmail.com");
        assertThat(user.getPassword()).isEqualTo("password456");
        assertThat(user.getPhone()).isEqualTo("0987654321");
        assertThat(user.getAddress()).isEqualTo("Hanoi");
        assertThat(user.getGoogleId()).isEqualTo("google-id-456");

        assertThat(user.getRole()).isSameAs(role);
        assertThat(user.getOrders()).isSameAs(orders);
        assertThat(user.getComments()).isSameAs(comments);
        assertThat(user.getRefreshTokens()).isSameAs(refreshTokens);
        assertThat(user.getCart()).isSameAs(cart);
    }

    @Test
    void noArgsConstructor_shouldInitializeFieldsCorrectly() {
        // Act
        User user = new User();

        // Assert
        assertThat(user.getId()).isZero();

        assertThat(user.getName()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getPhone()).isNull();
        assertThat(user.getAddress()).isNull();
        assertThat(user.getGoogleId()).isNull();

        assertThat(user.getRole()).isNull();
        assertThat(user.getOrders()).isNull();
        assertThat(user.getComments()).isNull();
        assertThat(user.getRefreshTokens()).isNull();
        assertThat(user.getCart()).isNull();
    }

    @Test
    void setOptionalFieldsToNull_shouldAllowNullValues() {
        // Arrange
        User user = new User();

        // Act
        user.setPassword(null);
        user.setPhone(null);
        user.setAddress(null);
        user.setGoogleId(null);

        // Assert
        assertThat(user.getPassword()).isNull();
        assertThat(user.getPhone()).isNull();
        assertThat(user.getAddress()).isNull();
        assertThat(user.getGoogleId()).isNull();
    }
}
