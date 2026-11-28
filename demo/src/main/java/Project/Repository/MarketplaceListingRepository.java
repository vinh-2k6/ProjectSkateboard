package Project.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Project.Model.Entity.MarketplaceListing;

@Repository
public interface MarketplaceListingRepository extends JpaRepository<MarketplaceListing, Integer> {

    List<MarketplaceListing> findByProduct_Id(Integer productId);
}
