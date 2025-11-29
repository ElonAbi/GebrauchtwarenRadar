package de.kleinanzeigen.app.searchprofile;

import de.kleinanzeigen.app.searchprofile.dto.SearchProfileRequest;
import de.kleinanzeigen.app.searchprofile.dto.SearchProfileResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search-profiles")
public class SearchProfileController {

    private final SearchProfileService service;
    private final de.kleinanzeigen.app.search.SearchExecutionService executionService;

    public SearchProfileController(SearchProfileService service,
            de.kleinanzeigen.app.search.SearchExecutionService executionService) {
        this.service = service;
        this.executionService = executionService;
    }

    @GetMapping
    public List<SearchProfileResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public SearchProfileResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<SearchProfileResponse> create(@Valid @RequestBody SearchProfileRequest request) {
        SearchProfileResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/search-profiles/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public SearchProfileResponse update(@PathVariable Long id, @Valid @RequestBody SearchProfileRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/execute")
    public de.kleinanzeigen.app.search.SearchResult execute(@PathVariable Long id) {
        return executionService.execute(id);
    }
}
