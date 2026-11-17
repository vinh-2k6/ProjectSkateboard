
package Project.Model.Entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void createOrder_shouldInitializeAllFields() {
        // Arrange
        User user = new User();
        List<OrderItem> orderItems = new ArrayList<>();
        Instant now = Instant.now();

        // Act
        Order order = new Order(
                1,
                500000,
                "PENDING",
                now,
                user,
                orderItems
        );

        // Assert
        assertThat(order.getId()).isEqualTo(1);
        assertThat(order.getTotalPrice()).isEqualTo(500000);
        assertThat(order.getStatus()).isEqualTo("PENDING");
        assertThat(order.getCreatedAt()).isEqualTo(now);
        assertThat(order.getUser()).isSameAs(user);
        assertThat(order.getOrderItems()).isSameAs(orderItems);
    }

    @Test
    void setOrder_shouldUpdateAllFields() {
        // Arrange
        Order order = new Order();

        User user = new User();
        List<OrderItem> orderItems = new ArrayList<>();
        Instant now = Instant.now();

        // Act
        order.setId(2);
        order.setTotalPrice(1000000);
        order.setStatus("COMPLETED");
        order.setCreatedAt(now);
        order.setUser(user);
        order.setOrderItems(orderItems);

        // Assert
        assertThat(order.getId()).isEqualTo(2);
        assertThat(order.getTotalPrice()).isEqualTo(1000000);
        assertThat(order.getStatus()).isEqualTo("COMPLETED");
        assertThat(order.getCreatedAt()).isEqualTo(now);
        assertThat(order.getUser()).isSameAs(user);
        assertThat(order.getOrderItems()).isSameAs(orderItems);
    }

    @Test
    void noArgsConstructor_shouldInitializePrimitiveFieldsToZero() {
        // Act
        Order order = new Order();

        // Assert
        assertThat(order.getId()).isZero();
        assertThat(order.getTotalPrice()).isZero();
        assertThat(order.getStatus()).isNull();
        assertThat(order.getCreatedAt()).isNull();
        assertThat(order.getUser()).isNull();
        assertThat(order.getOrderItems()).isNull();
    }
}
