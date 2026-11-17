
package Project.Model.Entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void createProduct_shouldInitializeAllFields() {
        // Arrange
        Brand brand = new Brand();
        Category category = new Category();

        List<Comment> comments = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();
        List<CartItem> cartItems = new ArrayList<>();

        // Act
        Product product = new Product(
                1,
                "Nike Air",
                "Running shoes",
                2000000,
                10,
                "nike.jpg",
                brand,
                category,
                comments,
                orderItems,
                cartItems
        );

        // Assert
        assertThat(product.getId()).isEqualTo(1);
        assertThat(product.getName()).isEqualTo("Nike Air");
        assertThat(product.getDescription()).isEqualTo("Running shoes");
        assertThat(product.getPrice()).isEqualTo(2000000);
        assertThat(product.getQuantity()).isEqualTo(10);
        assertThat(product.getImage()).isEqualTo("nike.jpg");

        assertThat(product.getBrand()).isSameAs(brand);
        assertThat(product.getCategory()).isSameAs(category);
        assertThat(product.getComments()).isSameAs(comments);
        assertThat(product.getOrderItems()).isSameAs(orderItems);
        assertThat(product.getCartItems()).isSameAs(cartItems);
    }

    @Test
    void setProduct_shouldUpdateAllFields() {
        // Arrange
        Product product = new Product();

        Brand brand = new Brand();
        Category category = new Category();

        List<Comment> comments = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();
        List<CartItem> cartItems = new ArrayList<>();

        // Act
        product.setId(2);
        product.setName("Adidas Ultraboost");
        product.setDescription("Running shoes");
        product.setPrice(2500000);
        product.setQuantity(20);
        product.setImage("adidas.jpg");

        product.setBrand(brand);
        product.setCategory(category);
        product.setComments(comments);
        product.setOrderItems(orderItems);
        product.setCartItems(cartItems);

        // Assert
        assertThat(product.getId()).isEqualTo(2);
        assertThat(product.getName()).isEqualTo("Adidas Ultraboost");
        assertThat(product.getDescription()).isEqualTo("Running shoes");
        assertThat(product.getPrice()).isEqualTo(2500000);
        assertThat(product.getQuantity()).isEqualTo(20);
        assertThat(product.getImage()).isEqualTo("adidas.jpg");

        assertThat(product.getBrand()).isSameAs(brand);
        assertThat(product.getCategory()).isSameAs(category);
        assertThat(product.getComments()).isSameAs(comments);
        assertThat(product.getOrderItems()).isSameAs(orderItems);
        assertThat(product.getCartItems()).isSameAs(cartItems);
    }

    @Test
    void noArgsConstructor_shouldInitializeFieldsAsNull() {
        // Act
        Product product = new Product();

        // Assert
        assertThat(product.getId()).isNull();
        assertThat(product.getName()).isNull();
        assertThat(product.getDescription()).isNull();
        assertThat(product.getPrice()).isNull();
        assertThat(product.getQuantity()).isNull();
        assertThat(product.getImage()).isNull();

        assertThat(product.getBrand()).isNull();
        assertThat(product.getCategory()).isNull();
        assertThat(product.getComments()).isNull();
        assertThat(product.getOrderItems()).isNull();
        assertThat(product.getCartItems()).isNull();
    }
}
