package de.kleinanzeigen.app.search;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchExecutionService searchExecutionService;

    public SearchController(SearchExecutionService searchExecutionService) {
        this.searchExecutionService = searchExecutionService;
    }

    @PostMapping("/profiles/{id}/execute")
    public ResponseEntity<SearchResult> execute(@PathVariable Long id) {
        SearchResult result = searchExecutionService.execute(id);
        return ResponseEntity.ok(result);
    }
}
