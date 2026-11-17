package Project.Model.Entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class BrandTest {

    @Test
    void createBrand_shouldInitializeAllFields() {
        // Arrange
        List<Product> products = new ArrayList<>();

        // Act
        Brand brand = new Brand(
                1,
                "Nike",
                "Sports brand",
                products
        );

        // Assert
        assertThat(brand.getId()).isEqualTo(1);
        assertThat(brand.getName()).isEqualTo("Nike");
        assertThat(brand.getDescription()).isEqualTo("Sports brand");
        assertThat(brand.getProducts()).isSameAs(products);
    }

    @Test
    void setBrand_shouldUpdateFields() {
        // Arrange
        Brand brand = new Brand();

        List<Product> products = new ArrayList<>();

        // Act
        brand.setId(1);
        brand.setName("Adidas");
        brand.setDescription("Sports brand");
        brand.setProducts(products);

        // Assert
        assertThat(brand.getId()).isEqualTo(1);
        assertThat(brand.getName()).isEqualTo("Adidas");
        assertThat(brand.getDescription()).isEqualTo("Sports brand");
        assertThat(brand.getProducts()).isSameAs(products);
    }

    @Test
    void noArgsConstructor_shouldInitializeFieldsAsNull() {
        // Act
        Brand brand = new Brand();

        // Assert
        assertThat(brand.getId()).isNull();
        assertThat(brand.getName()).isNull();
        assertThat(brand.getDescription()).isNull();
        assertThat(brand.getProducts()).isNull();
    }
}