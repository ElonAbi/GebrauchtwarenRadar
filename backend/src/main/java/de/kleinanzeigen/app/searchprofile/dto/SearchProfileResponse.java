package de.kleinanzeigen.app.searchprofile.dto;

import java.math.BigDecimal;

public record SearchProfileResponse(
        Long id,
        String name,
        String query,
        String category,
        String marketplaceId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer frequencyMinutes
) {
}
