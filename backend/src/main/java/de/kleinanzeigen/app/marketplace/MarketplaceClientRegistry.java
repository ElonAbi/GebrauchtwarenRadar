package de.kleinanzeigen.app.marketplace;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MarketplaceClientRegistry {

    private final List<MarketplaceClient> clients;

    public MarketplaceClientRegistry(List<MarketplaceClient> clients) {
        this.clients = clients;
    }

    public MarketplaceClient getClient(String marketplaceId) {
        return clients.stream()
                .filter(client -> client.supports(marketplaceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no client for marketplace " + marketplaceId));
    }
}
