package Project.Model.Entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class CommentTest {

    @Test
    void createComment_shouldInitializeAllFields() {
        // Arrange
        Product product = new Product();
        User user = new User();

        Instant now = Instant.now();

        // Act
        Comment comment = new Comment(
                1,
                product,
                user,
                4.5,
                "Good product",
                true,
                now,
                now
        );

        // Assert
        assertThat(comment.getId()).isEqualTo(1);
        assertThat(comment.getProduct()).isSameAs(product);
        assertThat(comment.getUser()).isSameAs(user);
        assertThat(comment.getRating()).isEqualTo(4.5);
        assertThat(comment.getContent()).isEqualTo("Good product");
        assertThat(comment.isApproved()).isTrue();
        assertThat(comment.getCreatedAt()).isEqualTo(now);
        assertThat(comment.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void setComment_shouldUpdateFields() {
        // Arrange
        Comment comment = new Comment();

        Product product = new Product();
        User user = new User();

        Instant now = Instant.now();

        // Act
        comment.setId(1);
        comment.setProduct(product);
        comment.setUser(user);
        comment.setRating(5.0);
        comment.setContent("Excellent");
        comment.setApproved(true);
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);

        // Assert
        assertThat(comment.getId()).isEqualTo(1);
        assertThat(comment.getProduct()).isSameAs(product);
        assertThat(comment.getUser()).isSameAs(user);
        assertThat(comment.getRating()).isEqualTo(5.0);
        assertThat(comment.getContent()).isEqualTo("Excellent");
        assertThat(comment.isApproved()).isTrue();
        assertThat(comment.getCreatedAt()).isEqualTo(now);
        assertThat(comment.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void setApproved_shouldUpdateApprovalStatus() {
        // Arrange
        Comment comment = new Comment();

        // Act
        comment.setApproved(true);

        // Assert
        assertThat(comment.isApproved()).isTrue();

        // Act
        comment.setApproved(false);

        // Assert
        assertThat(comment.isApproved()).isFalse();
    }

    @Test
    void noArgsConstructor_shouldInitializeFieldsAsDefaultValues() {
        // Act
        Comment comment = new Comment();

        // Assert
        assertThat(comment.getId()).isNull();
        assertThat(comment.getProduct()).isNull();
        assertThat(comment.getUser()).isNull();
        assertThat(comment.getRating()).isNull();
        assertThat(comment.getContent()).isNull();
        assertThat(comment.isApproved()).isFalse();
        assertThat(comment.getCreatedAt()).isNull();
        assertThat(comment.getUpdatedAt()).isNull();
    }
}