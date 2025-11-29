package de.kleinanzeigen.app.marketplace;

import de.kleinanzeigen.app.search.SearchResultItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EbayShopHtmlParser {

    private static final Logger log = LoggerFactory.getLogger(EbayShopHtmlParser.class);

    public List<SearchResultItem> parse(Document document, String locationName) {
        List<SearchResultItem> items = new ArrayList<>();

        // Try standard search items
        Elements standardItems = document.select(".s-item__wrapper");
        for (Element element : standardItems) {
            parseItem(element, items, ".s-item__title", ".s-item__link", ".s-item__price", locationName);
        }

        // Try shop card items (Shop view)
        if (items.isEmpty()) {
            Elements shopItems = document.select(".su-card-container");
            for (Element element : shopItems) {
                parseItem(element, items, ".s-card__title", ".s-card__link", ".s-card__price", locationName);
            }
        }

        return items;
    }

    private void parseItem(Element element, List<SearchResultItem> items, String titleSelector, String linkSelector,
            String priceSelector, String locationName) {
        try {
            String title = element.select(titleSelector).text()
                    .replace("Wird in neuem Fenster oder Tab geöffnet", "")
                    .trim();

            // Skip "Shop on eBay" or empty titles
            if (title.contains("Shop on eBay") || title.isBlank()) {
                return;
            }

            String url = element.select(linkSelector).attr("href");
            String priceText = element.select(priceSelector).text();

            // Extract ID from URL or generate one
            String id = extractIdFromUrl(url);
            if (id == null || id.isBlank()) {
                id = url; // Fallback
            }

            BigDecimal price = parsePrice(priceText);

            if (!url.isBlank()) {
                items.add(new SearchResultItem(id, title, url, price, locationName, Instant.now()));
            }
        } catch (Exception e) {
            log.warn("Failed to parse eBay item", e);
        }
    }

    private String extractIdFromUrl(String url) {
        try {
            // Example: https://www.ebay.de/itm/1234567890
            if (url.contains("/itm/")) {
                String[] parts = url.split("/itm/");
                if (parts.length > 1) {
                    String afterItm = parts[1];
                    int questionMarkIndex = afterItm.indexOf('?');
                    if (questionMarkIndex != -1) {
                        return afterItm.substring(0, questionMarkIndex);
                    }
                    return afterItm;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private BigDecimal parsePrice(String priceText) {
        if (priceText == null || priceText.isBlank()) {
            return null;
        }
        try {
            // Format: "EUR 12,34" or "12,34 €" or "$20.00"
            String cleaned = priceText.replaceAll("[^0-9,.]", "");
            // If it has comma as decimal separator
            if (cleaned.contains(",") && !cleaned.contains(".")) {
                cleaned = cleaned.replace(",", ".");
            } else if (cleaned.contains(",") && cleaned.contains(".")) {
                // assume 1.234,56 -> 1234.56
                cleaned = cleaned.replace(".", "").replace(",", ".");
            }
            return new BigDecimal(cleaned);
        } catch (Exception e) {
            return null;
        }
    }
}
