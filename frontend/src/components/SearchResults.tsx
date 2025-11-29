import { useMemo, useState } from "react";
import type { SearchResult } from "@/types";

export function SearchResults({ result }: { result: SearchResult | null }) {
  const [sortOrder, setSortOrder] = useState<"default" | "price_asc" | "price_desc">("default");

  const sortedItems = useMemo(() => {
    if (!result) return [];
    const items = [...result.items];

    if (sortOrder === "default") return items;

    return items.sort((a, b) => {
      if (sortOrder === "price_asc") {
        const pA = a.price === undefined ? Infinity : a.price;
        const pB = b.price === undefined ? Infinity : b.price;
        return pA - pB;
      } else {
        const pA = a.price === undefined ? -Infinity : a.price;
        const pB = b.price === undefined ? -Infinity : b.price;
        return pB - pA;
      }
    });
  }, [result, sortOrder]);

  return (
    <section className="card">
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: "1rem" }}>
        <h2>Letzte Ergebnisliste</h2>
        {result && result.items.length > 0 && (
          <label style={{ fontSize: "0.9rem", display: "flex", alignItems: "center", gap: "0.5rem", fontWeight: "normal" }}>
            Sortierung:
            <select
              value={sortOrder}
              onChange={(e) => setSortOrder(e.target.value as any)}
              style={{ padding: "0.3rem", fontSize: "0.9rem", width: "auto" }}
            >
              <option value="default">Standard</option>
              <option value="price_desc">Preis absteigend</option>
              <option value="price_asc">Preis aufsteigend</option>
            </select>
          </label>
        )}
      </div>

      {!result ? (
        <p className="muted">Noch keine manuelle Suche ausgefuehrt.</p>
      ) : (
        <>
          <p className="muted">Ausgefuehrt am {new Date(result.executedAt).toLocaleString()}</p>
          {sortedItems.length === 0 ? (
            <p>Keine Treffer fuer dieses Profil gefunden.</p>
          ) : (
            <ul className="result-list">
              {sortedItems.map((item) => {
                const formattedPrice = item.price !== undefined ? `${item.price.toFixed(2)} EUR` : "–";
                return (
                  <li key={item.id}>
                    <a href={item.url} target="_blank" rel="noreferrer">
                      <strong>{item.title}</strong>
                    </a>
                    <div className="result-meta">
                      <span className="result-price">{formattedPrice}</span>
                      {item.location && <small className="result-location">{item.location}</small>}
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </>
      )}
    </section>
  );
}
