package Project.Model.Enum;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public enum MarketplacePlatform {
    SHOPEE("Shopee", "https://shopee.vn/search?keyword="),
    LAZADA("Lazada", "https://www.lazada.vn/catalog/?q="),
    TIKI("Tiki", "https://tiki.vn/search?q=");

    private final String label;
    private final String searchPrefix;

    MarketplacePlatform(String label, String searchPrefix) {
        this.label = label;
        this.searchPrefix = searchPrefix;
    }

    public String getLabel() {
        return label;
    }

    public String searchUrl(String keyword) {
        String query = keyword == null ? "" : keyword.trim();
        return searchPrefix + URLEncoder.encode(query, StandardCharsets.UTF_8);
    }
}
