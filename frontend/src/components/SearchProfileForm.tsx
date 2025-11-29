import { FormEvent, useEffect, useMemo, useState } from "react";
import type { SearchProfile, SearchProfilePayload } from "@/types";

type Props = {
  onSubmit: (payload: SearchProfilePayload, options?: { execute?: boolean; clear?: boolean }) => void;
  loading?: boolean;
  initialValue?: SearchProfile;
  onCancel?: () => void;
  executing?: boolean;
  lastExecutedAt?: string;
};

const defaultMarketplace = "kleinanzeigen";

export function SearchProfileForm({
  onSubmit,
  loading,
  initialValue,
  onCancel,
  executing,
  lastExecutedAt
}: Props) {
  const [name, setName] = useState(initialValue?.name ?? "");
  const [query, setQuery] = useState(initialValue?.query ?? "");
  const [category, setCategory] = useState(initialValue?.category ?? "");
  const [marketplaceIds, setMarketplaceIds] = useState<string[]>(initialValue?.marketplaceIds ?? [defaultMarketplace]);
  const [minPrice, setMinPrice] = useState(initialValue?.minPrice?.toString() ?? "");
  const [maxPrice, setMaxPrice] = useState(initialValue?.maxPrice?.toString() ?? "");
  const [frequencyMinutes, setFrequencyMinutes] = useState(initialValue?.frequencyMinutes.toString() ?? "30");



  useEffect(() => {
    setName(initialValue?.name ?? "");
    setQuery(initialValue?.query ?? "");
    setCategory(initialValue?.category ?? "");
    setMarketplaceIds(initialValue?.marketplaceIds ?? [defaultMarketplace]);
    setMinPrice(initialValue?.minPrice?.toString() ?? "");
    setMaxPrice(initialValue?.maxPrice?.toString() ?? "");
    setFrequencyMinutes(initialValue?.frequencyMinutes.toString() ?? "30");
  }, [initialValue]);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    let finalName = name.trim();
    let finalQuery = query.trim();

    if (!finalName && !finalQuery) {
      return;
    }

    if (!finalName) finalName = finalQuery;
    if (!finalQuery) finalQuery = finalName;

    const payload: SearchProfilePayload = {
      name: finalName,
      query: finalQuery,
      category: category.trim() || undefined,
      marketplaceIds,
      minPrice: minPrice ? Number(minPrice) : 1,
      maxPrice: maxPrice ? Number(maxPrice) : 999,
      frequencyMinutes: Number(frequencyMinutes)
    };

    // Determine which action to take based on the button clicked
    // Cast to any to access submitter which is standard in modern browsers but might be missing in some React types
    const submitter = (event.nativeEvent as any).submitter as HTMLButtonElement | null;
    const action = submitter?.name;

    let options: { execute?: boolean; clear?: boolean } = {};

    if (action === "save_execute") {
      options = { execute: true };
    } else if (action === "save_new") {
      options = { clear: true };
    } else if (action === "save") {
      options = {};
    } else {
      // Fallback (e.g. Enter key might not set submitter in some browsers, or if programmatic)
      // Default to primary action behavior
      if (!initialValue?.id) {
        options = { clear: true };
      }
    }

    onSubmit(payload, options);
  };

  const handleMarketplaceChange = (id: string, checked: boolean) => {
    setMarketplaceIds(prev => {
      if (checked) {
        return [...prev, id];
      } else {
        return prev.filter(m => m !== id);
      }
    });
  };

  const lastExecutionLabel = useMemo(() => {
    if (!initialValue?.id) {
      return "Noch keine manuelle Suche ausgefuehrt.";
    }
    if (!lastExecutedAt) {
      return "Noch keine manuelle Suche ausgefuehrt.";
    }
    return `Letzte Ausfuehrung: ${new Date(lastExecutedAt).toLocaleString()}`;
  }, [initialValue?.id, lastExecutedAt]);

  const isEditing = Boolean(initialValue?.id);

  return (
    <form onSubmit={handleSubmit} className="card">
      <h2>{isEditing ? "Suchprofil bearbeiten" : "Neues Suchprofil"}</h2>
      <label>
        Name
        <input value={name} onChange={(e) => setName(e.target.value)} placeholder="Wird aus Suchbegriff übernommen wenn leer" />
      </label>
      <label>
        Suchbegriff
        <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Wird aus Name übernommen wenn leer" />
      </label>
      <label>
        Kategorie
        <input value={category} onChange={(e) => setCategory(e.target.value)} placeholder="Optional" />
      </label>
      <div style={{ display: "flex", flexDirection: "column", gap: "0.35rem", fontSize: "0.9rem" }}>
        Marktplätze
        <div style={{
          display: "flex",
          flexDirection: "column",
          gap: "0.5rem",
          marginTop: "0.2rem",
          border: "1px solid rgba(148, 163, 184, 0.4)",
          borderRadius: "0.5rem",
          padding: "0.6rem 0.8rem",
          backgroundColor: "rgba(15, 23, 42, 0.7)"
        }}>
          <div style={{ display: "flex", flexDirection: "row", alignItems: "center", gap: "0.5rem" }}>
            <input
              type="checkbox"
              id="mp-kleinanzeigen"
              checked={marketplaceIds.includes("kleinanzeigen")}
              onChange={(e) => handleMarketplaceChange("kleinanzeigen", e.target.checked)}
              style={{ width: "auto", margin: 0 }}
            />
            <label htmlFor="mp-kleinanzeigen" style={{ margin: 0, fontWeight: "normal", fontSize: "inherit", cursor: "pointer" }}>
              kleinanzeigen.de
            </label>
            <a href="https://www.kleinanzeigen.de" target="_blank" rel="noopener noreferrer" style={{ color: "inherit", display: "flex", alignItems: "center" }} onClick={(e) => e.stopPropagation()}>
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
                <polyline points="15 3 21 3 21 9"></polyline>
                <line x1="10" y1="14" x2="21" y2="3"></line>
              </svg>
            </a>
          </div>
          <div style={{ display: "flex", flexDirection: "row", alignItems: "center", gap: "0.5rem" }}>
            <input
              type="checkbox"
              id="mp-manayga"
              checked={marketplaceIds.includes("manayga")}
              onChange={(e) => handleMarketplaceChange("manayga", e.target.checked)}
              style={{ width: "auto", margin: 0 }}
            />
            <label htmlFor="mp-manayga" style={{ margin: 0, fontWeight: "normal", fontSize: "inherit", cursor: "pointer" }}>
              Manayga (Versandkostenfrei ab 50€)
            </label>
            <a href="https://manayga.de/" target="_blank" rel="noopener noreferrer" style={{ color: "inherit", display: "flex", alignItems: "center" }} onClick={(e) => e.stopPropagation()}>
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
                <polyline points="15 3 21 3 21 9"></polyline>
                <line x1="10" y1="14" x2="21" y2="3"></line>
              </svg>
            </a>
          </div>
          <div style={{ display: "flex", flexDirection: "row", alignItems: "center", gap: "0.5rem" }}>
            <input
              type="checkbox"
              id="mp-ebay_rebuy"
              checked={marketplaceIds.includes("ebay_rebuy")}
              onChange={(e) => handleMarketplaceChange("ebay_rebuy", e.target.checked)}
              style={{ width: "auto", margin: 0 }}
            />
            <label htmlFor="mp-ebay_rebuy" style={{ margin: 0, fontWeight: "normal", fontSize: "inherit", cursor: "pointer" }}>
              eBay Rebuy (20% bei 5 Artikeln)
            </label>
            <a href="https://www.ebay.de/str/rebuyshop" target="_blank" rel="noopener noreferrer" style={{ color: "inherit", display: "flex", alignItems: "center" }} onClick={(e) => e.stopPropagation()}>
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
                <polyline points="15 3 21 3 21 9"></polyline>
                <line x1="10" y1="14" x2="21" y2="3"></line>
              </svg>
            </a>
          </div>
          <div style={{ display: "flex", flexDirection: "row", alignItems: "center", gap: "0.5rem" }}>
            <input
              type="checkbox"
              id="mp-ebay_buchpark"
              checked={marketplaceIds.includes("ebay_buchpark")}
              onChange={(e) => handleMarketplaceChange("ebay_buchpark", e.target.checked)}
              style={{ width: "auto", margin: 0 }}
            />
            <label htmlFor="mp-ebay_buchpark" style={{ margin: 0, fontWeight: "normal", fontSize: "inherit", cursor: "pointer" }}>
              eBay Buchpark
            </label>
            <a href="https://www.ebay.de/str/buchparkausverkauf" target="_blank" rel="noopener noreferrer" style={{ color: "inherit", display: "flex", alignItems: "center" }} onClick={(e) => e.stopPropagation()}>
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
                <polyline points="15 3 21 3 21 9"></polyline>
                <line x1="10" y1="14" x2="21" y2="3"></line>
              </svg>
            </a>
          </div>
          <div style={{ display: "flex", flexDirection: "row", alignItems: "center", gap: "0.5rem" }}>
            <input
              type="checkbox"
              id="mp-ebay_worldofbooks"
              checked={marketplaceIds.includes("ebay_worldofbooks")}
              onChange={(e) => handleMarketplaceChange("ebay_worldofbooks", e.target.checked)}
              style={{ width: "auto", margin: 0 }}
            />
            <label htmlFor="mp-ebay_worldofbooks" style={{ margin: 0, fontWeight: "normal", fontSize: "inherit", cursor: "pointer" }}>
              eBay worldofbooksde
            </label>
            <a href="https://www.ebay.de/str/worldofbooksde" target="_blank" rel="noopener noreferrer" style={{ color: "inherit", display: "flex", alignItems: "center" }} onClick={(e) => e.stopPropagation()}>
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
                <polyline points="15 3 21 3 21 9"></polyline>
                <line x1="10" y1="14" x2="21" y2="3"></line>
              </svg>
            </a>
          </div>
        </div>
      </div>
      <div className="grid two-cols">
        <label>
          Min Preis
          <input type="number" value={minPrice} onChange={(e) => setMinPrice(e.target.value)} min={0} step="0.01" />
        </label>
        <label>
          Max Preis
          <input type="number" value={maxPrice} onChange={(e) => setMaxPrice(e.target.value)} min={0} step="0.01" />
        </label>
      </div>
      <label>
        Intervall (Minuten)
        <input
          type="number"
          value={frequencyMinutes}
          onChange={(e) => setFrequencyMinutes(e.target.value)}
          min={5}
          step={5}
          required
        />
      </label>
      <p className="info-line muted">{lastExecutionLabel}</p>
      <div className="actions vertical">
        {/* Button 1: Save (and Clear if creating) */}
        <button
          type="submit"
          name={isEditing ? "save" : "save_new"}
          disabled={loading}
        >
          {loading
            ? "Speichern..."
            : isEditing
              ? "Speichern"
              : "Neues Suchprofil anlegen"}
        </button>

        {/* Button 2: Save and Execute */}
        <button
          type="submit"
          name="save_execute"
          disabled={loading || executing}
        >
          {executing
            ? "Suche laeuft..."
            : isEditing
              ? "Speichern und Suche starten"
              : "Neues Suchprofil anlegen und Suche starten"}
        </button>

        {onCancel && (
          <button type="button" onClick={onCancel} disabled={loading} className="ghost">
            Abbrechen
          </button>
        )}
      </div>
    </form>
  );
}
