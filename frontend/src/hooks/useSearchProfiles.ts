import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  createSearchProfile,
  deleteSearchProfile,
  executeSearch,
  listSearchProfiles,
  updateSearchProfile
} from "@/api/searchProfiles";
import type { SearchProfilePayload } from "@/types";

export function useSearchProfiles() {
  const queryClient = useQueryClient();

  const listQuery = useQuery({
    queryKey: ["search-profiles"],
    queryFn: listSearchProfiles,
    staleTime: 5 * 60 * 1000
  });

  const createMutation = useMutation({
    mutationFn: (payload: SearchProfilePayload) => createSearchProfile(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["search-profiles"] })
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: SearchProfilePayload }) =>
      updateSearchProfile(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["search-profiles"] })
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteSearchProfile(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["search-profiles"] })
  });

  const executeMutation = useMutation({
    mutationFn: (id: number) => executeSearch(id)
  });

  return {
    listQuery,
    createMutation,
    updateMutation,
    deleteMutation,
    executeMutation
  };
}
