package Project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Project.Model.Entity.Cart;
import Project.Model.Entity.CartItem;

@Repository 
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    
}
