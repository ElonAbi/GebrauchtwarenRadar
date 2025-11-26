import { FormEvent, useEffect, useMemo, useState } from "react";
import type { SearchProfile, SearchProfilePayload } from "@/types";

type Props = {
  onSubmit: (payload: SearchProfilePayload) => void;
  loading?: boolean;
  initialValue?: SearchProfile;
  onCancel?: () => void;
  onExecute?: () => void;
  executing?: boolean;
  lastExecutedAt?: string;
};

const defaultMarketplace = "kleinanzeigen";

export function SearchProfileForm({
  onSubmit,
  loading,
  initialValue,
  onCancel,
  onExecute,
  executing,
  lastExecutedAt
}: Props) {
  const [name, setName] = useState(initialValue?.name ?? "");
  const [query, setQuery] = useState(initialValue?.query ?? "");
  const [category, setCategory] = useState(initialValue?.category ?? "");
  const [marketplaceId, setMarketplaceId] = useState(initialValue?.marketplaceId ?? defaultMarketplace);
  const [minPrice, setMinPrice] = useState(initialValue?.minPrice?.toString() ?? "");
  const [maxPrice, setMaxPrice] = useState(initialValue?.maxPrice?.toString() ?? "");
  const [frequencyMinutes, setFrequencyMinutes] = useState(initialValue?.frequencyMinutes.toString() ?? "30");

  useEffect(() => {
    setName(initialValue?.name ?? "");
    setQuery(initialValue?.query ?? "");
    setCategory(initialValue?.category ?? "");
    setMarketplaceId(initialValue?.marketplaceId ?? defaultMarketplace);
    setMinPrice(initialValue?.minPrice?.toString() ?? "");
    setMaxPrice(initialValue?.maxPrice?.toString() ?? "");
    setFrequencyMinutes(initialValue?.frequencyMinutes.toString() ?? "30");
  }, [initialValue]);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const payload: SearchProfilePayload = {
      name,
      query,
      category: category.trim() || undefined,
      marketplaceId,
      minPrice: minPrice ? Number(minPrice) : undefined,
      maxPrice: maxPrice ? Number(maxPrice) : undefined,
      frequencyMinutes: Number(frequencyMinutes)
    };
    onSubmit(payload);
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

  const canExecute = Boolean(onExecute && initialValue?.id);

  return (
    <form onSubmit={handleSubmit} className="card">
      <h2>Suchprofil</h2>
      <label>
        Name
        <input value={name} onChange={(e) => setName(e.target.value)} required />
      </label>
      <label>
        Suchbegriff
        <input value={query} onChange={(e) => setQuery(e.target.value)} required />
      </label>
      <label>
        Kategorie
        <input value={category} onChange={(e) => setCategory(e.target.value)} placeholder="Optional" />
      </label>
      <label>
        Marktplatz
        <select value={marketplaceId} onChange={(e) => setMarketplaceId(e.target.value)}>
          <option value="kleinanzeigen">kleinanzeigen.de</option>
        </select>
      </label>
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
        <button type="submit" disabled={loading}>
          {loading ? "Speichern..." : "Speichern"}
        </button>
        {canExecute && (
          <button type="button" onClick={onExecute} disabled={executing}>
            {executing ? "Suche laeuft..." : "Suche jetzt"}
          </button>
        )}
        {onCancel && (
          <button type="button" onClick={onCancel} disabled={loading} className="ghost">
            Abbrechen
          </button>
        )}
      </div>
    </form>
  );
}
