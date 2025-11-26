import type { SearchResult } from "@/types";

export function SearchResults({ result }: { result: SearchResult | null }) {
  return (
    <section className="card">
      <h2>Letzte Ergebnisliste</h2>
      {!result ? (
        <p className="muted">Noch keine manuelle Suche ausgefuehrt.</p>
      ) : (
        <>
          <p className="muted">Ausgefuehrt am {new Date(result.executedAt).toLocaleString()}</p>
          {result.items.length === 0 ? (
            <p>Keine Treffer fuer dieses Profil gefunden.</p>
          ) : (
            <ul className="result-list">
              {result.items.map((item) => {
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
