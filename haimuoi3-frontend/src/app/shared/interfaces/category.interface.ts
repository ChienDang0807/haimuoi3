export interface Category {
  globalCategoryId: string;
  name: string;
  slug: string;
  subtitle?: string | null;
  imageUrl: string;
  ctaText?: string | null;
  route: string;
  displayOrder?: number;
  isActive: boolean;
  metaData?: Record<string, any>;
}

// Helper type cho display
export interface CategoryDisplay extends Category {
  displaySubtitle: string;
  displayCtaText: string;
  navigationPath: string;
}
