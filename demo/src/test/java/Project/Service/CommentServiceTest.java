
package Project.Service;

import Project.Exception.ResourceNotFoundException;
import Project.Model.DTO.Response.CommentResponseDTO;
import Project.Model.Entity.Comment;
import Project.Model.Entity.User;
import Project.Model.Entity.Product;
import Project.Repository.CommentRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private Comment comment;
    private User user;
    private Product product;

    @BeforeEach
    void setUp() {

        // Create User
        user = new User();
        user.setId(1);

        // Create Product
        product = new Product();
        product.setId(10);

        // Create Comment
        comment = new Comment();
        comment.setId(100);
        comment.setContent("Great product!");
        comment.setUser(user);
        comment.setProduct(product);
        comment.setCreatedAt(Instant.parse("2026-02-01T10:00:00Z"));
        comment.setUpdatedAt(Instant.parse("2026-02-02T10:00:00Z"));
        comment.setApproved(false);
    }

    // =========================================
    // 1. CONVERT COMMENT TO DTO
    // =========================================

    @Test
    void convertCommentToDTO_shouldReturnCommentDTO() {

        // Arrange
        // comment đã được khởi tạo trong setUp()

        // Act
        CommentResponseDTO result =
                commentService.convertCommentToDTO(comment);

        // Assert
        assertNotNull(result);

        assertAll(
                () -> assertEquals(100, result.getId()),
                () -> assertEquals("Great product!", result.getContent()),
                () -> assertEquals(1, result.getUserId()),
                () -> assertEquals(10, result.getProductId()),
                () -> assertEquals(comment.getCreatedAt(), result.getCreatedAt()),
                () -> assertEquals(comment.getUpdatedAt(), result.getUpdatedAt()),
                () -> assertFalse(result.isApproved())
        );

        verifyNoInteractions(commentRepository);
    }

    // =========================================
    // 2. CREATE COMMENT
    // =========================================

    @Test
    void createComment_shouldSetApprovedTrueAndSaveComment() {

        // Arrange
        comment.setApproved(false);

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Comment result = commentService.createComment(comment);

        // Assert
        assertNotNull(result);
        assertTrue(result.isApproved());

        verify(commentRepository).save(comment);
    }

    // =========================================
    // 3. FETCH ALL COMMENTS
    // =========================================

    @Test
    void fetchComments_shouldReturnListOfCommentDTO() {

        // Arrange
        Comment comment2 = new Comment();
        comment2.setId(200);
        comment2.setContent("Nice quality!");
        comment2.setUser(user);
        comment2.setProduct(product);
        comment2.setCreatedAt(Instant.parse("2026-02-01T10:00:00Z"));
        comment2.setUpdatedAt(Instant.parse("2026-02-02T10:00:00Z"));
        comment2.setApproved(true);

        when(commentRepository.findAll())
                .thenReturn(List.of(comment, comment2));

        // Act
        List<CommentResponseDTO> result = commentService.fetchComments();

        // Assert
        assertEquals(2, result.size());

        assertAll(
                () -> assertEquals(100, result.get(0).getId()),
                () -> assertEquals("Great product!", result.get(0).getContent()),
                () -> assertEquals(1, result.get(0).getUserId()),
                () -> assertEquals(10, result.get(0).getProductId()),

                () -> assertEquals(200, result.get(1).getId()),
                () -> assertEquals("Nice quality!", result.get(1).getContent()),
                () -> assertTrue(result.get(1).isApproved())
        );

        verify(commentRepository).findAll();
    }

    @Test
    void fetchComments_shouldReturnEmptyListWhenNoCommentExists() {

        // Arrange
        when(commentRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<CommentResponseDTO> result = commentService.fetchComments();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(commentRepository).findAll();
    }

    // =========================================
    // 4. FETCH COMMENT BY ID
    // =========================================

    @Test
    void fetchCommentById_shouldReturnCommentDTOWhenFound() {

        // Arrange
        when(commentRepository.findById(100))
                .thenReturn(Optional.of(comment));

        // Act
        CommentResponseDTO result = commentService.fetchCommentById(100);

        // Assert
        assertNotNull(result);

        assertAll(
                () -> assertEquals(100, result.getId()),
                () -> assertEquals("Great product!", result.getContent()),
                () -> assertEquals(1, result.getUserId()),
                () -> assertEquals(10, result.getProductId()),
                () -> assertFalse(result.isApproved())
        );

        verify(commentRepository).findById(100);
    }

    @Test
    void fetchCommentById_shouldThrowExceptionWhenNotFound() {

        // Arrange
        when(commentRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> commentService.fetchCommentById(999)
                );

        assertEquals("Comment not found", exception.getMessage());

        verify(commentRepository).findById(999);
    }

    // =========================================
    // 5. UPDATE COMMENT BY ID
    // =========================================

    @Test
    void updateCommentById_shouldUpdateContentAndApprovedStatus() {

        // Arrange
        Comment inputComment = new Comment();
        inputComment.setContent("Updated comment!");
        inputComment.setApproved(true);

        when(commentRepository.findById(100))
                .thenReturn(Optional.of(comment));

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CommentResponseDTO result =
                commentService.updateCommentById(100, inputComment);

        // Assert
        assertAll(
                () -> assertEquals(100, result.getId()),
                () -> assertEquals("Updated comment!", result.getContent()),
                () -> assertTrue(result.isApproved()),
                () -> assertEquals(1, result.getUserId()),
                () -> assertEquals(10, result.getProductId())
        );

        verify(commentRepository).findById(100);
        verify(commentRepository).save(comment);
    }

    @Test
    void updateCommentById_shouldThrowExceptionWhenCommentNotFound() {

        // Arrange
        Comment inputComment = new Comment();
        inputComment.setContent("Updated comment!");
        inputComment.setApproved(true);

        when(commentRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> commentService.updateCommentById(999, inputComment)
                );

        assertEquals("Comment not found", exception.getMessage());

        verify(commentRepository, never()).save(any(Comment.class));
    }

    // =========================================
    // 6. DELETE COMMENT BY ID
    // =========================================

    @Test
    void deleteCommentById_shouldDeleteCommentWhenFound() {

        // Arrange
        when(commentRepository.findById(100))
                .thenReturn(Optional.of(comment));

        // Act
        commentService.deleteCommentById(100);

        // Assert
        verify(commentRepository).findById(100);
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteCommentById_shouldThrowExceptionWhenCommentNotFound() {

        // Arrange
        when(commentRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> commentService.deleteCommentById(999)
                );

        assertEquals("Comment not found", exception.getMessage());

        verify(commentRepository, never()).delete(any(Comment.class));
    }
}
