package Project.Model.DTO.Response;

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
public class MarketplaceOfferDTO {

    private String platform;

    private String platformLabel;

    /** Giá niêm yết chắc chắn: giá shop hoặc bản ghi đã lưu. */
    private Integer price;

    /** Khoảng giá AI khi chưa có bản ghi sàn. */
    private Integer priceMin;

    private Integer priceMax;

    private Double rating;

    private Integer soldCount;

    private String shopName;

    private String listingTitle;

    private String url;

    private String highlights;

    /** STORE, CATALOG, ESTIMATE, SEARCH */
    private String source;

    private boolean ours;
}
