package Project.Model.Entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class CartTest {

    @Test
    void createCart_shouldInitializeAllFields() {
        // Arrange
        User user = new User();
        List<CartItem> items = new ArrayList<>();

        // Act
        Cart cart = new Cart(1, user, items);

        // Assert
        assertThat(cart.getId()).isEqualTo(1);
        assertThat(cart.getUser()).isSameAs(user);
        assertThat(cart.getItems()).isSameAs(items);
    }

    @Test
    void setCart_shouldUpdateFields() {
        // Arrange
        Cart cart = new Cart();

        User user = new User();
        List<CartItem> items = new ArrayList<>();

        // Act
        cart.setId(1);
        cart.setUser(user);
        cart.setItems(items);

        // Assert
        assertThat(cart.getId()).isEqualTo(1);
        assertThat(cart.getUser()).isSameAs(user);
        assertThat(cart.getItems()).isSameAs(items);
    }

    @Test
    void addCartItem_shouldStoreItemInCart() {
        // Arrange
        Cart cart = new Cart();
        CartItem item = new CartItem();

        List<CartItem> items = new ArrayList<>();

        // Act
        items.add(item);
        cart.setItems(items);

        // Assert
        assertThat(cart.getItems())
                .hasSize(1)
                .contains(item);
    }
}