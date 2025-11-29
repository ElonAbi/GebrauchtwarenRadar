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
          <label style={{ flexDirection: "row", alignItems: "center", gap: "0.5rem", fontSize: "inherit", fontWeight: "normal" }}>
            <input
              type="checkbox"
              checked={marketplaceIds.includes("kleinanzeigen")}
              onChange={(e) => handleMarketplaceChange("kleinanzeigen", e.target.checked)}
              style={{ width: "auto", margin: 0 }}
            />
            kleinanzeigen.de
          </label>
          <label style={{ flexDirection: "row", alignItems: "center", gap: "0.5rem", fontSize: "inherit", fontWeight: "normal" }}>
            <input
              type="checkbox"
              checked={marketplaceIds.includes("manayga")}
              onChange={(e) => handleMarketplaceChange("manayga", e.target.checked)}
              style={{ width: "auto", margin: 0 }}
            />
            Manayga
          </label>
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
