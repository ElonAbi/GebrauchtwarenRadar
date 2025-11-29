package de.kleinanzeigen.app.searchprofile.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SearchProfileRequest(
                @NotBlank String name,
                @NotBlank String query,
                String category,
                @NotNull java.util.List<String> marketplaceIds,
                BigDecimal minPrice,
                BigDecimal maxPrice,
                @NotNull @Min(5) Integer frequencyMinutes) {
}
