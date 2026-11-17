package Project.Model.Entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CartItemTest {

    @Test
    void createCartItem_shouldInitializeAllFields() {
        // Arrange
        Cart cart = new Cart();
        Product product = new Product();

        // Act
        CartItem item = new CartItem(
                1,
                cart,
                product,
                2,
                500000
        );

        // Assert
        assertThat(item.getId()).isEqualTo(1);
        assertThat(item.getCart()).isSameAs(cart);
        assertThat(item.getProduct()).isSameAs(product);
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getPrice()).isEqualTo(500000);
    }

    @Test
    void builder_shouldCreateCartItemCorrectly() {
        // Arrange
        Cart cart = new Cart();
        Product product = new Product();

        // Act
        CartItem item = CartItem.builder()
                .id(1)
                .cart(cart)
                .product(product)
                .quantity(3)
                .price(200000)
                .build();

        // Assert
        assertThat(item.getId()).isEqualTo(1);
        assertThat(item.getCart()).isSameAs(cart);
        assertThat(item.getProduct()).isSameAs(product);
        assertThat(item.getQuantity()).isEqualTo(3);
        assertThat(item.getPrice()).isEqualTo(200000);
    }

    @Test
    void setCartItem_shouldUpdateFields() {
        // Arrange
        CartItem item = new CartItem();

        Cart cart = new Cart();
        Product product = new Product();

        // Act
        item.setId(1);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(5);
        item.setPrice(100000);

        // Assert
        assertThat(item.getId()).isEqualTo(1);
        assertThat(item.getCart()).isSameAs(cart);
        assertThat(item.getProduct()).isSameAs(product);
        assertThat(item.getQuantity()).isEqualTo(5);
        assertThat(item.getPrice()).isEqualTo(100000);
    }
}