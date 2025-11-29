import { useEffect, useMemo, useState } from "react";
import { useSearchProfiles } from "@/hooks/useSearchProfiles";
import { SearchProfileForm } from "@/components/SearchProfileForm";
import { SearchProfilesList } from "@/components/SearchProfilesList";
import { SearchResults } from "@/components/SearchResults";
import type { SearchProfile, SearchProfilePayload, SearchResult } from "@/types";

function App() {
  const { listQuery, createMutation, updateMutation, deleteMutation, executeMutation } = useSearchProfiles();
  const profiles = listQuery.data ?? [];
  const [selectedProfileId, setSelectedProfileId] = useState<number | null>(null);
  const [editingProfile, setEditingProfile] = useState<SearchProfile | null>(null);
  const [latestResult, setLatestResult] = useState<SearchResult | null>(null);

  useEffect(() => {
    if (profiles.length === 0) {
      setSelectedProfileId(null);
      setEditingProfile(null);
      return;
    }
    // Only auto-select if we had a selection that is no longer valid
    if (selectedProfileId !== null && !profiles.some((profile) => profile.id === selectedProfileId)) {
      setSelectedProfileId(profiles[0].id);
    }
  }, [profiles, selectedProfileId]);

  const selectedProfile = useMemo(
    () => profiles.find((profile) => profile.id === selectedProfileId) ?? null,
    [profiles, selectedProfileId]
  );

  useEffect(() => {
    if (selectedProfile) {
      setEditingProfile(selectedProfile);
    } else if (profiles.length === 0) {
      setEditingProfile(null);
    }
  }, [selectedProfile, profiles.length]);

  const handleCreateOrUpdate = (
    payload: SearchProfilePayload,
    options?: { execute?: boolean; clear?: boolean }
  ) => {
    const onSuccess = (profile: SearchProfile) => {
      if (options?.clear) {
        setSelectedProfileId(null);
        setEditingProfile(null);
      } else {
        setSelectedProfileId(profile.id);
        setEditingProfile(profile);
      }

      if (options?.execute) {
        executeMutation.mutate(profile.id, {
          onSuccess: (result) => setLatestResult(result)
        });
      }
    };

    const onError = (error: Error) => {
      alert("Fehler beim Speichern: " + error.message);
    };

    if (editingProfile) {
      updateMutation.mutate(
        { id: editingProfile.id, payload },
        { onSuccess, onError }
      );
    } else {
      createMutation.mutate(payload, { onSuccess, onError });
    }
  };

  const handleDelete = (profile: SearchProfile) => {
    if (!confirm(`Profil ${profile.name} wirklich loeschen?`)) {
      return;
    }
    deleteMutation.mutate(profile.id, {
      onSuccess: () => {
        if (selectedProfileId === profile.id) {
          setSelectedProfileId(null);
        }
        if (editingProfile?.id === profile.id) {
          setEditingProfile(null);
        }
      }
    });
  };

  const handleExecute = () => {
    if (!editingProfile?.id) {
      return;
    }
    executeMutation.mutate(editingProfile.id, {
      onSuccess: (result) => setLatestResult(result)
    });
  };

  const handleSelect = (profile: SearchProfile) => {
    setSelectedProfileId(profile.id);
    setEditingProfile(profile);
  };

  const lastExecutedAt =
    latestResult && editingProfile && latestResult.searchProfileId === editingProfile.id
      ? latestResult.executedAt
      : undefined;

  return (
    <>
      <header className="app-header">
        <h1>Kleinanzeigen Radar</h1>
        <p className="muted">Behalte deine Wunsch-Listings im Blick</p>
      </header>
      <main className="layout-grid">
        <section className="search-form">
          <SearchProfileForm
            onSubmit={handleCreateOrUpdate}
            loading={createMutation.isPending || updateMutation.isPending}
            initialValue={editingProfile ?? undefined}
            onCancel={editingProfile ? () => { setEditingProfile(null); setSelectedProfileId(null); } : undefined}
            executing={executeMutation.isPending}
            lastExecutedAt={lastExecutedAt}
          />
        </section>
        <SearchResults result={latestResult} />
        <section className="card compact profile-list">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
            <h2 style={{ margin: 0 }}>Profil auswaehlen</h2>
            <button
              type="button"
              onClick={handleExecute}
              disabled={!selectedProfileId || executeMutation.isPending}
              style={{ fontSize: "0.9rem", padding: "0.4rem 0.8rem" }}
            >
              {executeMutation.isPending ? "Suche laeuft..." : "Suche starten"}
            </button>
          </div>
          {listQuery.isLoading ? (
            <p className="muted">Lade Profile...</p>
          ) : (
            <SearchProfilesList
              profiles={profiles}
              onSelect={handleSelect}
              onDelete={handleDelete}
              selectedId={selectedProfileId ?? undefined}
              isDeleting={deleteMutation.isPending}
            />
          )}
        </section>
      </main>
    </>
  );
}

export default App;
