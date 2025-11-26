package de.kleinanzeigen.app.marketplace;

import static org.assertj.core.api.Assertions.assertThat;

import de.kleinanzeigen.app.search.SearchResultItem;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

class KleinanzeigenHtmlParserTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");

    @Test
    void parsesSearchResults() throws IOException {
        Clock clock = Clock.fixed(Instant.parse("2025-09-27T10:00:00Z"), ZONE_ID);
        KleinanzeigenHtmlParser parser = new KleinanzeigenHtmlParser(clock);
        Document document = loadFixture("/fixtures/kleinanzeigen-search-results.html");

        List<SearchResultItem> items = parser.parse(document);

        assertThat(items).hasSize(3);
        assertThat(items).extracting(SearchResultItem::id)
                .containsExactlyInAnyOrder("123456789", "987654321", "111222333");

        SearchResultItem gamingPc = findById(items, "123456789");
        assertThat(gamingPc.title()).isEqualTo("Gaming PC RX6800");
        assertThat(gamingPc.url()).isEqualTo("https://www.kleinanzeigen.de/s-anzeige/gaming-pc-rx6800/123456789-228-4567");
        assertThat(gamingPc.price()).isNotNull().isEqualByComparingTo("1200");
        assertThat(gamingPc.location()).isEqualTo("Berlin Charlottenburg");
        assertThat(gamingPc.publishedAt()).isEqualTo(Instant.parse("2025-09-27T10:45:00Z"));

        SearchResultItem sofa = findById(items, "987654321");
        assertThat(sofa.price()).isEqualByComparingTo("250");
        assertThat(sofa.publishedAt()).isEqualTo(Instant.parse("2025-09-26T07:15:00Z"));

        SearchResultItem giveaway = findById(items, "111222333");
        assertThat(giveaway.price()).isNull();
    }

    private Document loadFixture(String path) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Fixture not found: " + path);
            }
            return Jsoup.parse(inputStream, StandardCharsets.UTF_8.name(), "https://www.kleinanzeigen.de");
        }
    }

    private SearchResultItem findById(List<SearchResultItem> items, String id) {
        return items.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
