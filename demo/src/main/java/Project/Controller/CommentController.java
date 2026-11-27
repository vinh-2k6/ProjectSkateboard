package Project.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Project.Helper.ApiResponse;
import Project.Model.Entity.Comment;
import Project.Model.DTO.Response.CommentResponseDTO;
import Project.Service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {
	private final CommentService commentService;

	@PostMapping("/comments")
	public ResponseEntity<ApiResponse<CommentResponseDTO>> createComment(@Valid @RequestBody Comment inputComment) {
		CommentResponseDTO newComment = this.commentService.createComment(inputComment);
		return ApiResponse.created(newComment);
	}

	@GetMapping("/products/{productId}/comments")
	public ResponseEntity<ApiResponse<List<CommentResponseDTO>>> getComments(@PathVariable Integer productId) {
		List<CommentResponseDTO> comments = this.commentService.fetchComments(productId);
		return ApiResponse.success(comments);
	}

	@GetMapping("/comments/{id}")
	public ResponseEntity<ApiResponse<CommentResponseDTO>> getCommentById(@PathVariable int id) {
		CommentResponseDTO comment = this.commentService.fetchCommentById(id);
		return ApiResponse.success(comment);
	}

	@PutMapping("/products/{productId}/comments/{id}")
	public ResponseEntity<ApiResponse<CommentResponseDTO>> updateCommentById(@PathVariable int id,
			@Valid @RequestBody Comment inputComment) {
		CommentResponseDTO comment = this.commentService.updateCommentById(id, inputComment);
		return ApiResponse.success(comment);
	}

	@DeleteMapping("/comments/{id}")
	public ResponseEntity<ApiResponse<String>> deleteCommentById(@PathVariable int id) {
		this.commentService.deleteCommentById(id);
		return ApiResponse.success("ok");
	}
}
