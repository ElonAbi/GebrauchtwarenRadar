import type { SearchProfile, SearchResult } from "@/types";

type Props = {
  profile: SearchProfile | null;
  onExecute: () => void;
  onEdit: () => void;
  result: SearchResult | null;
  executing?: boolean;
};

export function SearchExecutionPanel({ profile, onExecute, onEdit, result, executing }: Props) {
  if (!profile) {
    return (
      <section className="card">
        <h2>Manuelle Suche</h2>
        <p className="muted">Waehle ein Suchprofil, um eine manuelle Suche zu starten.</p>
      </section>
    );
  }

  const isSameResult = result && result.searchProfileId === profile.id;

  return (
    <section className="card">
      <header className="panel-header">
        <div>
          <h2>{profile.name}</h2>
          <p className="muted">{profile.query}</p>
        </div>
        <div className="actions">
          <button type="button" className="ghost" onClick={onEdit}>
            Bearbeiten
          </button>
          <button type="button" onClick={onExecute} disabled={executing}>
            {executing ? "Suche laeuft..." : "Suche jetzt"}
          </button>
        </div>
      </header>
      <p className="muted">
        Intervall {profile.frequencyMinutes} min | Marktplatz {profile.marketplaceId}
      </p>
      {isSameResult ? (
        <p className="muted">Letzte Ausfuehrung: {new Date(result!.executedAt).toLocaleString()}</p>
      ) : (
        <p className="muted">Noch keine manuelle Ausfuehrung fuer dieses Profil.</p>
      )}
    </section>
  );
}
