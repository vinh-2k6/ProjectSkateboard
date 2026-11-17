package Project.Model.Validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.Test;

import Project.Model.Entity.Product;

class ProductValidationTest {

    private final ValidatorFactory factory =
            Validation.buildDefaultValidatorFactory();

    private final Validator validator = factory.getValidator();

    @Test
    void validateProduct_shouldPassWhenAllFieldsAreValid() {
        // Arrange
        Product product = new Product();
        product.setName("Nike Air");
        product.setDescription("Running shoes");
        product.setPrice(2000000);
        product.setQuantity(10);
        product.setImage("nike.jpg");

        // Act
        Set<ConstraintViolation<Product>> violations =
                validator.validate(product);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void validateProduct_shouldReturnViolationsWhenRequiredFieldsAreBlank() {
        // Arrange
        Product product = new Product();
        product.setName("");
        product.setDescription(" ");
        product.setPrice(null);
        product.setQuantity(null);
        product.setImage("");

        // Act
        Set<ConstraintViolation<Product>> violations =
                validator.validate(product);

        // Assert
        assertThat(violations).hasSize(5);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "name không được để trống",
                        "description không được để trống",
                        "Price không được để trống",
                        "Quantity không được để trống",
                        "image không được để trống"
                );
    }

    @Test
    void validateProduct_shouldReturnViolationsWhenPriceIsNegative() {
        // Arrange
        Product product = createValidProduct();
        product.setPrice(-1);

        // Act
        Set<ConstraintViolation<Product>> violations =
                validator.validate(product);

        // Assert
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Price phải lớn hơn hoặc bằng 0");
    }

    @Test
    void validateProduct_shouldReturnViolationsWhenQuantityIsLessThanOne() {
        // Arrange
        Product product = createValidProduct();
        product.setQuantity(0);

        // Act
        Set<ConstraintViolation<Product>> violations =
                validator.validate(product);

        // Assert
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Quantity phải lớn hơn hoặc bằng 1");
    }

    private Product createValidProduct() {
        Product product = new Product();
        product.setName("Nike Air");
        product.setDescription("Running shoes");
        product.setPrice(2000000);
        product.setQuantity(10);
        product.setImage("nike.jpg");

        return product;
    }
}
