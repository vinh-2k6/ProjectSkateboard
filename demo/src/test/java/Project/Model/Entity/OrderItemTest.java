
package Project.Model.Entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrderItemTest {

    @Test
    void createOrderItem_shouldInitializeAllFields() {
        // Arrange
        Order order = new Order();
        Product product = new Product();

        // Act
        OrderItem item = new OrderItem(
                1,
                order,
                product,
                2,
                300000
        );

        // Assert
        assertThat(item.getId()).isEqualTo(1);
        assertThat(item.getOrder()).isSameAs(order);
        assertThat(item.getProduct()).isSameAs(product);
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getPrice()).isEqualTo(300000);
    }

    @Test
    void setOrderItem_shouldUpdateAllFields() {
        // Arrange
        OrderItem item = new OrderItem();

        Order order = new Order();
        Product product = new Product();

        // Act
        item.setId(2);
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(5);
        item.setPrice(200000);

        // Assert
        assertThat(item.getId()).isEqualTo(2);
        assertThat(item.getOrder()).isSameAs(order);
        assertThat(item.getProduct()).isSameAs(product);
        assertThat(item.getQuantity()).isEqualTo(5);
        assertThat(item.getPrice()).isEqualTo(200000);
    }

    @Test
    void noArgsConstructor_shouldInitializePrimitiveFieldsToZero() {
        // Act
        OrderItem item = new OrderItem();

        // Assert
        assertThat(item.getId()).isZero();
        assertThat(item.getQuantity()).isZero();
        assertThat(item.getPrice()).isZero();
        assertThat(item.getOrder()).isNull();
        assertThat(item.getProduct()).isNull();
    }
}
