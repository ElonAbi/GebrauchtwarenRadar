package de.kleinanzeigen.app.searchprofile;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "search_profiles")
public class SearchProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String query;

    private String category;

    @NotBlank
    @Column(name = "marketplace_id", nullable = false)
    private String marketplaceId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "min", column = @Column(name = "min_price")),
            @AttributeOverride(name = "max", column = @Column(name = "max_price"))
    })
    private PriceRange priceRange;

    @NotNull
    @Positive
    @Column(name = "frequency_minutes", nullable = false)
    private Integer frequencyMinutes;

    protected SearchProfile() {
        // for JPA
    }

    private SearchProfile(String name, String query, String category, String marketplaceId, PriceRange priceRange, Integer frequencyMinutes) {
        this.name = name;
        this.query = query;
        this.category = category;
        this.marketplaceId = marketplaceId;
        this.priceRange = priceRange;
        this.frequencyMinutes = frequencyMinutes;
    }

    public static SearchProfile create(String name, String query, String category, String marketplaceId, PriceRange priceRange, Integer frequencyMinutes) {
        return new SearchProfile(name, query, category, marketplaceId, priceRange, frequencyMinutes);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getQuery() {
        return query;
    }

    public String getCategory() {
        return category;
    }

    public String getMarketplaceId() {
        return marketplaceId;
    }

    public PriceRange getPriceRange() {
        return priceRange;
    }

    public Integer getFrequencyMinutes() {
        return frequencyMinutes;
    }

    public void update(String name, String query, String category, String marketplaceId, PriceRange priceRange, Integer frequencyMinutes) {
        this.name = name;
        this.query = query;
        this.category = category;
        this.marketplaceId = marketplaceId;
        this.priceRange = priceRange;
        this.frequencyMinutes = frequencyMinutes;
    }
}

