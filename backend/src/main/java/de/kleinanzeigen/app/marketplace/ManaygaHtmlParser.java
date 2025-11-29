package de.kleinanzeigen.app.marketplace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.kleinanzeigen.app.search.SearchResultItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ManaygaHtmlParser {

    private static final Logger log = LoggerFactory.getLogger(ManaygaHtmlParser.class);
    private static final String BASE_URL = "https://manayga.de";
    private final ObjectMapper objectMapper;

    public ManaygaHtmlParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<SearchResultItem> parse(Document document) {
        // Try parsing from data-events script first
        Optional<List<SearchResultItem>> jsonItems = parseFromJson(document);
        if (jsonItems.isPresent()) {
            return jsonItems.get();
        }

        // Try parsing from script content (web-pixels-manager-setup)
        jsonItems = parseFromScriptContent(document);
        if (jsonItems.isPresent()) {
            return jsonItems.get();
        }

        // Fallback to HTML parsing
        return parseFromHtml(document);
    }

    private Optional<List<SearchResultItem>> parseFromJson(Document document) {
        Element script = document.selectFirst("script[data-events]");
        if (script == null) {
            return Optional.empty();
        }

        String dataEvents = script.attr("data-events");
        if (dataEvents.isBlank()) {
            return Optional.empty();
        }

        return parseEventsJson(dataEvents);
    }

    private Optional<List<SearchResultItem>> parseFromScriptContent(Document document) {
        Element script = document.getElementById("web-pixels-manager-setup");
        if (script == null) {
            return Optional.empty();
        }

        String content = script.data();
        if (content.isBlank()) {
            return Optional.empty();
        }

        // Extract events string: "events":"..." or events:"..."
        // Regex caused StackOverflow on large strings, so we use manual extraction
        String[] markers = { "\"events\":\"", "events:\"" };
        int start = -1;
        for (String marker : markers) {
            start = content.indexOf(marker);
            if (start != -1) {
                start += marker.length();
                break;
            }
        }

        if (start != -1) {
            StringBuilder jsonBuilder = new StringBuilder();
            boolean escaped = false;
            for (int i = start; i < content.length(); i++) {
                char c = content.charAt(i);
                if (escaped) {
                    jsonBuilder.append(c);
                    escaped = false;
                } else {
                    if (c == '\\') {
                        escaped = true;
                    } else if (c == '"') {
                        break; // End of string
                    } else {
                        jsonBuilder.append(c);
                    }
                }
            }
            return parseEventsJson(jsonBuilder.toString());
        }

        return Optional.empty();
    }

    private Optional<List<SearchResultItem>> parseEventsJson(String jsonString) {
        try {
            JsonNode root = objectMapper.readTree(jsonString);
            if (!root.isArray()) {
                return Optional.empty();
            }

            for (JsonNode event : root) {
                if (event.isArray() && event.size() >= 2 && "search_submitted".equals(event.get(0).asText())) {
                    JsonNode payload = event.get(1);
                    if (payload.has("searchResult") && payload.get("searchResult").has("productVariants")) {
                        return Optional.of(extractItemsFromJson(payload.get("searchResult").get("productVariants")));
                    }
                }
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse events JSON", e);
        }
        return Optional.empty();
    }

    private List<SearchResultItem> extractItemsFromJson(JsonNode productVariants) {
        List<SearchResultItem> items = new ArrayList<>();
        if (!productVariants.isArray()) {
            return items;
        }

        for (JsonNode variant : productVariants) {
            try {
                String id = variant.path("product").path("id").asText();
                String title = variant.path("product").path("title").asText();
                String urlPath = variant.path("product").path("url").asText();
                double priceValue = variant.path("price").path("amount").asDouble();

                if (id.isBlank() || title.isBlank()) {
                    continue;
                }

                String url = urlPath.startsWith("http") ? urlPath
                        : BASE_URL + (urlPath.startsWith("/") ? "" : "/") + urlPath;
                BigDecimal price = BigDecimal.valueOf(priceValue);

                items.add(new SearchResultItem(id, title, url, price, "Manayga", Instant.now()));
            } catch (Exception e) {
                log.warn("Failed to parse item from JSON", e);
            }
        }
        return items;
    }

    private List<SearchResultItem> parseFromHtml(Document document) {
        List<SearchResultItem> items = new ArrayList<>();
        Elements containers = document.select(".grid-view-item, .product-card, .product-item");

        for (Element container : containers) {
            try {
                String title = container
                        .select(".grid-view-item__title, .product-card__title, .product-item__title, h3 a").text();
                String url = container.select("a").attr("href");
                String priceText = container.select(".price-item--regular, .money").text();

                if (title.isBlank() || url.isBlank()) {
                    continue;
                }

                String fullUrl = url.startsWith("http") ? url : BASE_URL + (url.startsWith("/") ? "" : "/") + url;
                BigDecimal price = parsePrice(priceText);
                String id = fullUrl; // Use URL as ID if no better ID found

                items.add(new SearchResultItem(id, title, fullUrl, price, "Manayga", Instant.now()));
            } catch (Exception e) {
                log.warn("Failed to parse item from HTML", e);
            }
        }
        return items;
    }

    private BigDecimal parsePrice(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            String cleaned = text.replaceAll("[^0-9,.]", "").replace(",", ".");
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
