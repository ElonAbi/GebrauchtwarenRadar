package de.kleinanzeigen.app.marketplace;

import de.kleinanzeigen.app.search.SearchResultItem;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.Normalizer;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class KleinanzeigenHtmlParser {

    private static final Logger log = LoggerFactory.getLogger(KleinanzeigenHtmlParser.class);

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");
    private static final Pattern PRICE_PATTERN = Pattern.compile("([0-9\\s.\\u00A0]+[,][0-9]{2}|[0-9\\s.\\u00A0]+)");
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2})[:.](\\d{2})");
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{1,2})[.](\\d{1,2})[.](\\d{2,4})");

    private final Clock clock;

    public KleinanzeigenHtmlParser() {
        this(Clock.system(ZONE_ID));
    }

    public KleinanzeigenHtmlParser(Clock clock) {
        this.clock = clock;
    }

    public List<SearchResultItem> parse(Document document) {
        List<SearchResultItem> items = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        Elements containers = document.select("li.aditem, article[data-adid]");

        for (Element container : containers) {
            extractItem(container).ifPresent(item -> {
                if (seenIds.add(item.id())) {
                    items.add(item);
                }
            });
        }
        return items;
    }

    private Optional<SearchResultItem> extractItem(Element container) {
        String id = extractId(container);
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        Element titleElement = firstNonNull(
                container.selectFirst("h2"),
                container.selectFirst("h3"),
                container.selectFirst("a.ellipsis"));
        if (titleElement == null) {
            return Optional.empty();
        }
        String title = titleElement.text().trim();
        if (title.isBlank()) {
            return Optional.empty();
        }

        Element linkElement = container.selectFirst("a[href]");
        String href = linkElement != null ? linkElement.attr("href") : null;
        if (href == null || href.isBlank()) {
            return Optional.empty();
        }
        String url = normalizeUrl(href);

        BigDecimal price = extractPrice(container);
        String location = extractLocation(container);
        Instant publishedAt = extractPublishedAt(container);

        return Optional.of(new SearchResultItem(id, title, url, price, location, publishedAt));
    }

    private String extractId(Element container) {
        if (container.hasAttr("data-adid")) {
            return container.attr("data-adid");
        }
        if (container.hasAttr("data-item-id")) {
            return container.attr("data-item-id");
        }
        Element idHolder = container.selectFirst("[name=adId]");
        return idHolder != null ? idHolder.attr("value") : null;
    }

    private BigDecimal extractPrice(Element container) {
        Element priceElement = firstNonNull(
                container.selectFirst(".aditem-main--middle--price"),
                container.selectFirst(".aditem-main--middle__price"),
                container.selectFirst(".aditem-main--middle--price-shipping--price"),
                container.selectFirst(".aditem-main--price"),
                container.selectFirst(".price"));
        if (priceElement == null) {
            log.debug("Price element missing for listing");
            return null;
        }
        String raw = priceElement.text();
        if (raw == null) {
            return null;
        }
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC).toLowerCase(Locale.GERMAN);
        if (normalized.contains("verschenken")) {
            return null;
        }
        String sanitized = normalized
                .replace("vb", "")
                .replaceAll("[^0-9,\\.\\s\\u00A0]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        Matcher matcher = PRICE_PATTERN.matcher(sanitized);
        if (!matcher.find()) {
            log.debug("Could not parse price - raw='{}' sanitized='{}'", raw, sanitized);
            return null;
        }
        String numeric = matcher.group(1)
                .replace("\u00A0", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(",", ".")
                .trim();
        if (numeric.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(numeric);
        } catch (NumberFormatException ignored) {
            log.debug("Number format exception for price '{}'", numeric);
            return null;
        }
    }

    private String extractLocation(Element container) {
        Element locationCandidate = firstNonNull(
                container.selectFirst(".aditem-main--bottom span"),
                container.selectFirst(".aditem-main--bottom div"),
                container.selectFirst(".aditem-main--top span"));
        if (locationCandidate == null) {
            return null;
        }
        String text = locationCandidate.text();
        if (text == null) {
            return null;
        }
        String cleaned = text.strip();
        return cleaned.isBlank() ? null : cleaned;
    }

    private Instant extractPublishedAt(Element container) {
        Elements dateCandidates = container.select(".aditem-main--bottom *");
        Instant now = Instant.now(clock);
        for (Element candidate : dateCandidates) {
            String text = candidate.text();
            if (text == null || text.isBlank()) {
                continue;
            }
            Instant parsed = parsePublishedAt(text);
            if (parsed != null) {
                return parsed;
            }
        }
        return now;
    }

    private Instant parsePublishedAt(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String lower = trimmed.toLowerCase(Locale.GERMAN);
        LocalDate baseDate = LocalDate.now(clock);
        if (lower.startsWith("heute")) {
            LocalTime time = extractTime(trimmed).orElse(LocalTime.of(0, 0));
            return ZonedDateTime.of(baseDate, time, ZONE_ID).toInstant();
        }
        if (lower.startsWith("gestern")) {
            LocalTime time = extractTime(trimmed).orElse(LocalTime.of(0, 0));
            return ZonedDateTime.of(baseDate.minusDays(1), time, ZONE_ID).toInstant();
        }
        Matcher dateMatcher = DATE_PATTERN.matcher(trimmed);
        if (dateMatcher.find()) {
            int day = Integer.parseInt(dateMatcher.group(1));
            int month = Integer.parseInt(dateMatcher.group(2));
            int year = Integer.parseInt(dateMatcher.group(3));
            if (year < 100) {
                year += 2000;
            }
            LocalDate date = LocalDate.of(year, month, day);
            LocalTime time = extractTime(trimmed).orElse(LocalTime.of(0, 0));
            return ZonedDateTime.of(LocalDateTime.of(date, time), ZONE_ID).toInstant();
        }
        return null;
    }

    private Optional<LocalTime> extractTime(String text) {
        Matcher timeMatcher = TIME_PATTERN.matcher(text);
        if (timeMatcher.find()) {
            int hour = Integer.parseInt(timeMatcher.group(1));
            int minute = Integer.parseInt(timeMatcher.group(2));
            if (hour >= 0 && hour < 24 && minute >= 0 && minute < 60) {
                return Optional.of(LocalTime.of(hour, minute));
            }
        }
        return Optional.empty();
    }

    @SafeVarargs
    private Element firstNonNull(Element... candidates) {
        for (Element element : candidates) {
            if (element != null) {
                return element;
            }
        }
        return null;
    }

    private String normalizeUrl(String href) {
        if (href.startsWith("http://") || href.startsWith("https://")) {
            return href;
        }
        String normalized = href.startsWith("/") ? href : "/" + href;
        return "https://www.kleinanzeigen.de" + normalized;
    }
}
