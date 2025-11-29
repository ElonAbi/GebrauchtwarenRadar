export type SearchProfile = {
  id: number;
  name: string;
  query: string;
  category?: string;
  marketplaceIds: string[];
  minPrice?: number;
  maxPrice?: number;
  frequencyMinutes: number;
};

export type SearchProfilePayload = {
  name: string;
  query: string;
  category?: string;
  marketplaceIds: string[];
  minPrice?: number;
  maxPrice?: number;
  frequencyMinutes: number;
};

export type SearchResultItem = {
  id: string;
  title: string;
  url: string;
  price?: number;
  location?: string;
  publishedAt?: string;
};

export type SearchResult = {
  searchProfileId: number;
  executedAt: string;
  items: SearchResultItem[];
};
