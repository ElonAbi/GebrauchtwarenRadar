package de.kleinanzeigen.app.marketplace;

import de.kleinanzeigen.app.search.SearchResultItem;
import de.kleinanzeigen.app.searchprofile.SearchProfile;
import java.util.List;

public interface MarketplaceClient {

    boolean supports(String marketplaceId);

    List<SearchResultItem> search(SearchProfile profile);
}
