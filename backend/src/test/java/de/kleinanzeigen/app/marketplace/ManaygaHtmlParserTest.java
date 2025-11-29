package de.kleinanzeigen.app.marketplace;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.kleinanzeigen.app.search.SearchResultItem;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

class ManaygaHtmlParserTest {

    private final ManaygaHtmlParser parser = new ManaygaHtmlParser(new ObjectMapper());

    @Test
    void shouldParseItemsFromDataEvents() {
        String html = """
                <html>
                <body>
                <script async="" src="test.js"
                    data-events="[[&quot;page_viewed&quot;,{}],[&quot;search_submitted&quot;,{&quot;searchResult&quot;:{&quot;query&quot;:&quot;dragon&quot;,&quot;productVariants&quot;:[{&quot;price&quot;:{&quot;amount&quot;:4.0,&quot;currencyCode&quot;:&quot;EUR&quot;},&quot;product&quot;:{&quot;title&quot;:&quot;Dragon Ball Massiv 01&quot;,&quot;vendor&quot;:&quot;Manayga&quot;,&quot;id&quot;:&quot;9433955991875&quot;,&quot;untranslatedTitle&quot;:&quot;Dragon Ball Massiv 01&quot;,&quot;url&quot;:&quot;/products/dragon-ball-massiv-1-manayga?_pos=1&amp;_sid=a2b53ca21&amp;_ss=r&quot;,&quot;type&quot;:&quot;Manga&quot;},&quot;id&quot;:&quot;48901133664579&quot;,&quot;image&quot;:{&quot;src&quot;:&quot;//manayga.de/cdn/shop/files/853AEBDC-5340-4CC6-8814-6019359E1AC5.png?v=1720466735&quot;},&quot;sku&quot;:&quot;DRAGOBAL-001S&quot;,&quot;title&quot;:&quot;Sehr Gut&quot;,&quot;untranslatedTitle&quot;:&quot;Sehr Gut&quot;}]}}]]">
                </script>
                </body>
                </html>
                """;

        Document document = Jsoup.parse(html);
        List<SearchResultItem> items = parser.parse(document);

        assertThat(items).hasSize(1);
        SearchResultItem item = items.get(0);
        assertThat(item.title()).isEqualTo("Dragon Ball Massiv 01");
        assertThat(item.price()).isNotNull();
        assertThat(item.price().doubleValue()).isEqualTo(4.0);
    }

    @Test
    void shouldParseItemsFromScriptContent() {
        String html = """
                <html>
                <body>
                <script id="web-pixels-manager-setup">
                    (function e(e,d,r,n,o){
                        var initData = {
                            events: "[[\\"search_submitted\\",{\\"searchResult\\":{\\"productVariants\\":[{\\"price\\":{\\"amount\\":4.0},\\"product\\":{\\"id\\":\\"123\\",\\"title\\":\\"Test Product\\",\\"url\\":\\"/p/1\\"}}]}}]]"
                        };
                    })(window);
                </script>
                </body>
                </html>
                """;

        Document document = Jsoup.parse(html);
        List<SearchResultItem> items = parser.parse(document);

        assertThat(items).hasSize(1);
        SearchResultItem item = items.get(0);
        assertThat(item.title()).isEqualTo("Test Product");
        assertThat(item.price()).isNotNull();
        assertThat(item.price().doubleValue()).isEqualTo(4.0);
    }
}
