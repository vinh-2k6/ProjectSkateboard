package Project.Model.Entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class CategoryTest {

    @Test
    void createCategory_shouldInitializeAllFields() {
        // Arrange
        List<Product> products = new ArrayList<>();

        // Act
        Category category = new Category(
                1,
                "Shoes",
                "Sports shoes",
                products
        );

        // Assert
        assertThat(category.getId()).isEqualTo(1);
        assertThat(category.getName()).isEqualTo("Shoes");
        assertThat(category.getDescription()).isEqualTo("Sports shoes");
        assertThat(category.getProduct()).isSameAs(products);
    }

    @Test
    void setCategory_shouldUpdateFields() {
        // Arrange
        Category category = new Category();

        List<Product> products = new ArrayList<>();

        // Act
        category.setId(2);
        category.setName("Clothing");
        category.setDescription("Sports clothing");
        category.setProduct(products);

        // Assert
        assertThat(category.getId()).isEqualTo(2);
        assertThat(category.getName()).isEqualTo("Clothing");
        assertThat(category.getDescription()).isEqualTo("Sports clothing");
        assertThat(category.getProduct()).isSameAs(products);
    }

    @Test
    void noArgsConstructor_shouldInitializeFieldsAsNull() {
        // Act
        Category category = new Category();

        // Assert
        assertThat(category.getId()).isNull();
        assertThat(category.getName()).isNull();
        assertThat(category.getDescription()).isNull();
        assertThat(category.getProduct()).isNull();
    }
}