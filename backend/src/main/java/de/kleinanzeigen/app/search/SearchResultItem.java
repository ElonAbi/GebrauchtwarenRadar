package de.kleinanzeigen.app.search;

import java.math.BigDecimal;
import java.time.Instant;

public record SearchResultItem(
        String id,
        String title,
        String url,
        BigDecimal price,
        String location,
        Instant publishedAt
) {
}
