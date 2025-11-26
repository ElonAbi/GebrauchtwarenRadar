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
            ResultFilterService resultFilterService
    ) {
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
        MarketplaceClient client = marketplaceClientRegistry.getClient(profile.getMarketplaceId());
        List<SearchResultItem> items = client.search(profile);
        List<SearchResultItem> filtered = resultFilterService.applyFilters(profile, items);
        return new SearchResult(profile.getId(), Instant.now(), filtered);
    }
}
