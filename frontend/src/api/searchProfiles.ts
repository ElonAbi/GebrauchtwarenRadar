import { z } from "zod";
import { apiClient } from "./client";
import type { SearchProfile, SearchProfilePayload, SearchResult } from "@/types";

const searchProfileSchema = z.object({
  id: z.number(),
  name: z.string(),
  query: z.string(),
  category: z.string().nullish(),
  marketplaceId: z.string(),
  minPrice: z.number().nullish(),
  maxPrice: z.number().nullish(),
  frequencyMinutes: z.number()
});

const searchProfilesSchema = z.array(searchProfileSchema);

const searchResultSchema = z.object({
  searchProfileId: z.number(),
  executedAt: z.string(),
  items: z
    .array(
      z.object({
        id: z.string(),
        title: z.string(),
        url: z.string(),
        price: z.number().nullish(),
        location: z.string().nullish(),
        publishedAt: z.string().nullish()
      })
    )
    .default([])
});

export async function listSearchProfiles(): Promise<SearchProfile[]> {
  const { data } = await apiClient.get("/search-profiles");
  return searchProfilesSchema.parse(data).map((entry) => ({
    ...entry,
    category: entry.category ?? undefined,
    minPrice: entry.minPrice ?? undefined,
    maxPrice: entry.maxPrice ?? undefined
  }));
}

export async function createSearchProfile(payload: SearchProfilePayload): Promise<SearchProfile> {
  const { data } = await apiClient.post("/search-profiles", payload);
  const parsed = searchProfileSchema.parse(data);
  return {
    ...parsed,
    category: parsed.category ?? undefined,
    minPrice: parsed.minPrice ?? undefined,
    maxPrice: parsed.maxPrice ?? undefined
  };
}

export async function updateSearchProfile(id: number, payload: SearchProfilePayload): Promise<SearchProfile> {
  const { data } = await apiClient.put(`/search-profiles/${id}`, payload);
  const parsed = searchProfileSchema.parse(data);
  return {
    ...parsed,
    category: parsed.category ?? undefined,
    minPrice: parsed.minPrice ?? undefined,
    maxPrice: parsed.maxPrice ?? undefined
  };
}

export async function deleteSearchProfile(id: number): Promise<void> {
  await apiClient.delete(`/search-profiles/${id}`);
}

export async function executeSearch(id: number): Promise<SearchResult> {
  const { data } = await apiClient.post(`/search/profiles/${id}/execute`);
  const parsed = searchResultSchema.parse(data);
  return {
    ...parsed,
    items: parsed.items.map((item) => ({
      ...item,
      price: item.price ?? undefined,
      location: item.location ?? undefined,
      publishedAt: item.publishedAt ?? undefined
    }))
  };
}
