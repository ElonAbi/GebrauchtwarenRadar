package de.kleinanzeigen.app.searchprofile;

import de.kleinanzeigen.app.search.SearchJobScheduler;
import de.kleinanzeigen.app.searchprofile.dto.SearchProfileRequest;
import de.kleinanzeigen.app.searchprofile.dto.SearchProfileResponse;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SearchProfileService {

    private final SearchProfileRepository repository;
    private final SearchJobScheduler jobScheduler;

    public SearchProfileService(SearchProfileRepository repository, SearchJobScheduler jobScheduler) {
        this.repository = repository;
        this.jobScheduler = jobScheduler;
    }

    public List<SearchProfileResponse> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public SearchProfileResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new SearchProfileNotFoundException(id));
    }

    @Transactional
    public SearchProfileResponse create(SearchProfileRequest request) {
        repository.findByName(request.name()).ifPresent(existing -> {
            throw new DuplicateSearchProfileException(existing.getName());
        });
        SearchProfile profile = SearchProfile.create(
                request.name(),
                request.query(),
                request.category(),
                request.marketplaceIds(),
                PriceRange.of(request.minPrice(), request.maxPrice()),
                request.frequencyMinutes());
        SearchProfile saved = repository.save(profile);
        jobScheduler.scheduleProfile(saved);
        return toResponse(saved);
    }

    @Transactional
    public SearchProfileResponse update(Long id, SearchProfileRequest request) {
        SearchProfile profile = repository.findById(id)
                .orElseThrow(() -> new SearchProfileNotFoundException(id));
        profile.update(
                request.name(),
                request.query(),
                request.category(),
                request.marketplaceIds(),
                PriceRange.of(request.minPrice(), request.maxPrice()),
                request.frequencyMinutes());
        jobScheduler.scheduleProfile(profile);
        return toResponse(profile);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new SearchProfileNotFoundException(id);
        }
        jobScheduler.cancelProfile(id);
        repository.deleteById(id);
    }

    private SearchProfileResponse toResponse(SearchProfile profile) {
        PriceRange priceRange = profile.getPriceRange();
        return new SearchProfileResponse(
                profile.getId(),
                profile.getName(),
                profile.getQuery(),
                profile.getCategory(),
                profile.getMarketplaceIds(),
                priceRange != null ? priceRange.getMin() : null,
                priceRange != null ? priceRange.getMax() : null,
                profile.getFrequencyMinutes());
    }
}
