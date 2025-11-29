package de.kleinanzeigen.app.marketplace;

import de.kleinanzeigen.app.search.SearchResultItem;
import de.kleinanzeigen.app.searchprofile.SearchProfile;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ManaygaClient implements MarketplaceClient {

    private static final Logger log = LoggerFactory.getLogger(ManaygaClient.class);
    public static final String MARKETPLACE_ID = "manayga";
    private static final String BASE_URL = "https://manayga.de";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/129.0 Safari/537.36";

    private final ManaygaHtmlParser parser;

    public ManaygaClient(ManaygaHtmlParser parser) {
        this.parser = parser;
    }

    @Override
    public boolean supports(String marketplaceId) {
        return MARKETPLACE_ID.equalsIgnoreCase(marketplaceId);
    }

    @Override
    public List<SearchResultItem> search(SearchProfile profile) {
        List<SearchResultItem> allItems = new ArrayList<>();
        int page = 1;
        int maxPages = 5; // Limit to avoid excessive scraping

        while (page <= maxPages) {
            throttle();
            String requestUrl = buildSearchUrl(profile, page);
            try {
                log.debug("Fetching Manayga listing for profile {} from {}", profile.getId(), requestUrl);
                Document document = loadDocument(requestUrl);
                List<SearchResultItem> items = parser.parse(document);

                if (items.isEmpty()) {
                    break;
                }

                allItems.addAll(items);
                page++;

                // If we found fewer items than a typical page size, we're likely done
                if (items.size() < 12) {
                    break;
                }
            } catch (IOException e) {
                log.error("Failed to load search page from Manayga: {}", requestUrl, e);
                break; // Stop on error
            }
        }

        log.debug("Fetched total {} items for profile {}", allItems.size(), profile.getId());
        return allItems;
    }

    private Document loadDocument(String url) throws IOException {
        Connection connection = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout((int) Duration.ofSeconds(15).toMillis())
                .header("Accept-Language", "de-DE,de;q=0.9,en;q=0.8")
                .header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Cache-Control", "no-cache")
                .followRedirects(true);
        return connection.get();
    }

    private String buildSearchUrl(SearchProfile profile, int page) {
        String query = URLEncoder.encode(profile.getQuery(), StandardCharsets.UTF_8);
        return BASE_URL + "/search?q=" + query + "&page=" + page;
    }

    private void throttle() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
