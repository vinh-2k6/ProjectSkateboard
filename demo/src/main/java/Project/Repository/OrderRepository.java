package Project.Repository;



import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Project.Model.Entity.Order;

@Repository 
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("""
        SELECT o
        FROM Order o
        JOIN o.user u
        WHERE u.id = :userId
        ORDER BY o.createdAt DESC
    """)
    List<Order> searchOrdersOfUser(@Param("userId") Integer userId);

    Optional<Order> findById(Integer id);
}
