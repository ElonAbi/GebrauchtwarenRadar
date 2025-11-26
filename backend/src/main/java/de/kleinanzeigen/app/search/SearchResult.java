package de.kleinanzeigen.app.search;

import java.time.Instant;
import java.util.List;

public record SearchResult(
        Long searchProfileId,
        Instant executedAt,
        List<SearchResultItem> items
) {
}
