package Project.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Project.Model.Entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    @Query("""
        SELECT c
        FROM Comment c
        INNER JOIN c.product p
        WHERE p.id = :productID
        ORDER BY c.createdAt DESC
    """)
    List<Comment> searchCommentOfProduct(@Param("productID") Integer productId);
}
