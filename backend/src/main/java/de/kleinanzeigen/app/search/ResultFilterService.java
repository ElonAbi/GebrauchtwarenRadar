package de.kleinanzeigen.app.search;

import de.kleinanzeigen.app.searchprofile.PriceRange;
import de.kleinanzeigen.app.searchprofile.SearchProfile;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ResultFilterService {

    public List<SearchResultItem> applyFilters(SearchProfile profile, List<SearchResultItem> items) {
        PriceRange priceRange = profile.getPriceRange();
        if (priceRange == null) {
            return items;
        }
        return items.stream()
                .filter(item -> priceRange.contains(item.price()))
                .toList();
    }
}
