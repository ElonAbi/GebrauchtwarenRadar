package de.kleinanzeigen.app.marketplace;

import de.kleinanzeigen.app.search.SearchResultItem;
import de.kleinanzeigen.app.searchprofile.SearchProfile;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EbayRebuyClient implements MarketplaceClient {

    private static final Logger log = LoggerFactory.getLogger(EbayRebuyClient.class);
    // Base URL provided by user:
    // https://www.ebay.de/sch/i.html?_dkr=1&iconV2Request=true&_blrs=recall_filtering&_ssn=rebuy-shop&store_cat=0&store_name=rebuyshop&_oac=1&_nkw=dragon%20ball%20manga
    private static final String BASE_URL = "https://www.ebay.de/sch/i.html?_dkr=1&iconV2Request=true&_blrs=recall_filtering&_ssn=rebuy-shop&store_cat=0&store_name=rebuyshop&_oac=1&_nkw=";

    private final EbayShopHtmlParser parser;

    public EbayRebuyClient(EbayShopHtmlParser parser) {
        this.parser = parser;
    }

    @Override
    public boolean supports(String marketplaceId) {
        return "ebay_rebuy".equals(marketplaceId);
    }

    @Override
    public List<SearchResultItem> search(SearchProfile profile) {
        try {
            String encodedQuery = URLEncoder.encode(profile.getQuery(), StandardCharsets.UTF_8);
            String url = BASE_URL + encodedQuery;

            log.info("Fetching eBay Rebuy results for query: {}", profile.getQuery());

            Document document = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept-Language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7")
                    .get();

            return parser.parse(document, "eBay Rebuy");
        } catch (IOException e) {
            log.error("Failed to fetch results from eBay Rebuy", e);
            return Collections.emptyList();
        }
    }
}
