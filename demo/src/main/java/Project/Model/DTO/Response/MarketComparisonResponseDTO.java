package Project.Model.DTO.Response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketComparisonResponseDTO {

    private boolean resolved;

    private String summary;

    private String verdict;

    private String disclaimer;

    private ProductResponseDTO product;

    private List<MarketplaceOfferDTO> offers;
}
