package de.kleinanzeigen.app.marketplace;

import de.kleinanzeigen.app.search.SearchResultItem;
import de.kleinanzeigen.app.searchprofile.SearchProfile;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ManualEbayShopsTest {

    @Test
    public void runManualTest() {
        EbayShopHtmlParser parser = new EbayShopHtmlParser();

        testClient(new EbayRebuyClient(parser), "ebay_rebuy");
        testClient(new EbayBuchparkClient(parser), "ebay_buchpark");
        testClient(new EbayWorldOfBooksClient(parser), "ebay_worldofbooks");
    }

    private void testClient(MarketplaceClient client, String marketplaceId) {
        System.out.println("Testing " + marketplaceId + "...");

        SearchProfile profile = SearchProfile.create(
                "Test Profile",
                "dragon ball manga",
                null,
                Collections.singletonList(marketplaceId),
                null,
                30);

        List<SearchResultItem> items = client.search(profile);
        System.out.println("Found items: " + items.size());

        for (int i = 0; i < Math.min(items.size(), 3); i++) {
            System.out.println(items.get(i));
        }
        System.out.println("--------------------------------------------------");
    }
}
