package de.kleinanzeigen.app.searchprofile;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class PriceRange {

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal min;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal max;

    protected PriceRange() {
        // for JPA
    }

    private PriceRange(BigDecimal min, BigDecimal max) {
        this.min = min;
        this.max = max;
    }

    public static PriceRange of(BigDecimal min, BigDecimal max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException("min price must be less or equal to max price");
        }
        return new PriceRange(min, max);
    }

    public BigDecimal getMin() {
        return min;
    }

    public BigDecimal getMax() {
        return max;
    }

    public boolean contains(BigDecimal price) {
        if (price == null) {
            return true;
        }
        if (min != null && price.compareTo(min) < 0) {
            return false;
        }
        if (max != null && price.compareTo(max) > 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PriceRange that)) {
            return false;
        }
        return Objects.equals(min, that.min) && Objects.equals(max, that.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }
}
