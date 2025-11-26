package de.kleinanzeigen.app.marketplace;

import de.kleinanzeigen.app.search.SearchResultItem;
import de.kleinanzeigen.app.searchprofile.PriceRange;
import de.kleinanzeigen.app.searchprofile.SearchProfile;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KleinanzeigenClient implements MarketplaceClient {

    private static final Logger log = LoggerFactory.getLogger(KleinanzeigenClient.class);
    public static final String MARKETPLACE_ID = "kleinanzeigen";
    private static final String BASE_URL = "https://www.kleinanzeigen.de";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/129.0 Safari/537.36";
    private static final Duration MIN_REQUEST_INTERVAL = Duration.ofSeconds(3);

    private final KleinanzeigenHtmlParser parser;
    private final Object throttleLock = new Object();
    private Instant lastRequest = Instant.EPOCH;

    public KleinanzeigenClient(KleinanzeigenHtmlParser parser) {
        this.parser = parser;
    }

    @Override
    public boolean supports(String marketplaceId) {
        return MARKETPLACE_ID.equalsIgnoreCase(marketplaceId);
    }

    @Override
    public List<SearchResultItem> search(SearchProfile profile) {
        throttle();
        String requestUrl = buildSearchUrl(profile);
        try {
            log.debug("Fetching Kleinanzeigen listing for profile {} from {}", profile.getId(), requestUrl);
            Document document = loadDocument(requestUrl);
            List<SearchResultItem> items = parser.parse(document);
            log.debug("Fetched {} items for profile {}", items.size(), profile.getId());
            return items;
        } catch (IOException e) {
            throw new MarketplaceSearchException("Failed to load search page from Kleinanzeigen", e);
        }
    }

    private Document loadDocument(String url) throws IOException {
        Connection connection = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout((int) Duration.ofSeconds(15).toMillis())
                .header("Accept-Language", "de-DE,de;q=0.9,en;q=0.8")
                .header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Cache-Control", "no-cache")
                .referrer("https://www.google.com")
                .followRedirects(true);
        return connection.get();
    }

    private String buildSearchUrl(SearchProfile profile) {
        String categorySegment = safeSlug(profile.getCategory());
        String querySegment = safeSlug(profile.getQuery());
        if (querySegment.isBlank()) {
            querySegment = "angebote";
        }
        String priceSegment = buildPriceSegment(profile.getPriceRange());

        StringBuilder pathBuilder = new StringBuilder(BASE_URL).append("/s");
        if (!categorySegment.isBlank()) {
            pathBuilder.append('-').append(categorySegment);
        }
        if (!priceSegment.isBlank()) {
            pathBuilder.append('-').append(priceSegment);
        }
        pathBuilder.append('/').append(querySegment).append("/k0");
        return pathBuilder.toString();
    }

    private String buildPriceSegment(PriceRange priceRange) {
        if (priceRange == null) {
            return "";
        }
        String minPart = priceRange.getMin() != null ? formatPrice(priceRange.getMin()) : "";
        String maxPart = priceRange.getMax() != null ? formatPrice(priceRange.getMax()) : "";
        if (minPart.isEmpty() && maxPart.isEmpty()) {
            return "";
        }
        return "preis:" + minPart + ':' + maxPart;
    }

    private void throttle() {
        synchronized (throttleLock) {
            Instant now = Instant.now();
            Instant earliestNext = lastRequest.plus(MIN_REQUEST_INTERVAL);
            if (now.isBefore(earliestNext)) {
                Duration waitDuration = Duration.between(now, earliestNext);
                try {
                    Thread.sleep(waitDuration.toMillis());
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            lastRequest = Instant.now();
        }
    }

    private String safeSlug(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim().toLowerCase(Locale.GERMAN), Normalizer.Form.NFKD)
                .replaceAll("\\p{M}+", "");
        return normalized.replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String formatPrice(BigDecimal price) {
        return price.stripTrailingZeros().toPlainString();
    }
}
