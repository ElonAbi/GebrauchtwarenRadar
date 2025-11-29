package de.kleinanzeigen.app.search;

import de.kleinanzeigen.app.marketplace.MarketplaceClient;
import de.kleinanzeigen.app.marketplace.MarketplaceClientRegistry;
import de.kleinanzeigen.app.searchprofile.SearchProfile;
import de.kleinanzeigen.app.searchprofile.SearchProfileRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SearchExecutionService {

    private final SearchProfileRepository searchProfileRepository;
    private final MarketplaceClientRegistry marketplaceClientRegistry;
    private final ResultFilterService resultFilterService;

    public SearchExecutionService(
            SearchProfileRepository searchProfileRepository,
            MarketplaceClientRegistry marketplaceClientRegistry,
            ResultFilterService resultFilterService) {
        this.searchProfileRepository = searchProfileRepository;
        this.marketplaceClientRegistry = marketplaceClientRegistry;
        this.resultFilterService = resultFilterService;
    }

    public SearchResult execute(Long searchProfileId) {
        SearchProfile profile = searchProfileRepository.findById(searchProfileId)
                .orElseThrow(() -> new IllegalArgumentException("search profile not found: " + searchProfileId));
        return execute(profile);
    }

    public SearchResult execute(SearchProfile profile) {
        java.util.List<SearchResultItem> allItems = new java.util.ArrayList<>();

        for (String marketplaceId : profile.getMarketplaceIds()) {
            try {
                MarketplaceClient client = marketplaceClientRegistry.getClient(marketplaceId);
                List<SearchResultItem> items = client.search(profile);
                allItems.addAll(items);
            } catch (Exception e) {
                // Log error but continue with other marketplaces
                // You might want to log this properly
                System.err.println("Failed to search marketplace " + marketplaceId + ": " + e.getMessage());
            }
        }

        List<SearchResultItem> filtered = resultFilterService.applyFilters(profile, allItems);
        return new SearchResult(profile.getId(), Instant.now(), filtered);
    }
}
