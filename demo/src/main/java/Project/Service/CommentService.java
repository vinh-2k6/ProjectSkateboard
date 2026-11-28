package Project.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import Project.Exception.ResourceNotFoundException;
import Project.Model.Entity.Comment;
import Project.Model.Entity.Product;
import Project.Model.Entity.User;
import Project.Model.DTO.Response.CommentResponseDTO;
import Project.Repository.CommentRepository;
import Project.Repository.ProductRepository;
import Project.Repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // Convert Comment -> CommentResponseDTO
    public CommentResponseDTO convertCommentToDTO(Comment comment) {

        CommentResponseDTO dto = new CommentResponseDTO();

        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        if (comment.getUser() != null) {
            dto.setUserId(comment.getUser().getId());
            dto.setUserName(comment.getUser().getName());
        }
        if (comment.getProduct() != null) {
            dto.setProductId(comment.getProduct().getId());
        }
        dto.setRating(comment.getRating() != null ? comment.getRating() : 5.0);
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setApproved(comment.getIsApproved() != null ? comment.getIsApproved() : true);

        return dto;
    }

    // Create comment
    public CommentResponseDTO createComment(Comment comment) {
        if (comment.getUser() != null && comment.getUser().getId() != 0) {
            User user = this.userRepository.findById(comment.getUser().getId()).orElse(null);
            comment.setUser(user);
        }
        if (comment.getProduct() != null && comment.getProduct().getId() != null) {
            Product product = this.productRepository.findById(comment.getProduct().getId()).orElse(null);
            comment.setProduct(product);
        }
        if (comment.getRating() == null) {
            comment.setRating(5.0);
        }
        comment.setIsApproved(true);
        return convertCommentToDTO(this.commentRepository.save(comment));
    }

    // Get all comments
    public List<CommentResponseDTO> fetchComments(Integer productId) {
        List<CommentResponseDTO> commentResponseDTOs = this.commentRepository
                                .searchCommentOfProduct(productId)
                                .stream()
                                .map(comment -> {
                                    return convertCommentToDTO(comment);
                                })
                                .collect(Collectors.toList());
        return commentResponseDTOs;
    }

    // Get comment by ID
    public CommentResponseDTO fetchCommentById(Integer id) {

        Comment comment = this.commentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Comment not found"));

        return convertCommentToDTO(comment);
    }

    // Update comment
    public CommentResponseDTO updateCommentById(
            Integer id,
            Comment inputComment) {

        Comment comment = this.commentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Comment not found"));

        comment.setContent(inputComment.getContent());
        comment.setIsApproved(inputComment.getIsApproved());

        Comment updatedComment = this.commentRepository.save(comment);

        return convertCommentToDTO(updatedComment);
    }

    // Delete comment
    public void deleteCommentById(Integer id) {

        Comment comment = this.commentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Comment not found"));

        this.commentRepository.delete(comment);
    }
}