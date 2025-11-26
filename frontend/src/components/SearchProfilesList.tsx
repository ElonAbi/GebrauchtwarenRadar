import type { SearchProfile } from "@/types";

type Props = {
  profiles: SearchProfile[];
  onSelect: (profile: SearchProfile) => void;
  onDelete: (profile: SearchProfile) => void;
  selectedId?: number;
  isDeleting?: boolean;
};

export function SearchProfilesList({ profiles, onSelect, onDelete, selectedId, isDeleting }: Props) {
  if (!profiles.length) {
    return <p className="muted">Noch keine Suchprofile angelegt.</p>;
  }

  return (
    <ul className="list">
      {profiles.map((profile) => (
        <li key={profile.id} className={profile.id === selectedId ? "active" : undefined}>
          <button type="button" onClick={() => onSelect(profile)}>
            <strong>{profile.name}</strong>
            <span>{profile.query}</span>
            <small>{profile.frequencyMinutes} min</small>
          </button>
          <button type="button" className="danger" disabled={isDeleting} onClick={() => onDelete(profile)}>
            Loeschen
          </button>
        </li>
      ))}
    </ul>
  );
}
