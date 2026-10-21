package Project.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Project.Model.Entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    
}
