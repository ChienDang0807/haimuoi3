import { Category, CategoryDisplay } from '../interfaces/category.interface';

/**
 * Prepare category for display với fallback logic
 * Theo guide: docs/FE_CATEGORY_INTEGRATION.md
 */
export function prepareCategoryForDisplay(category: Category): CategoryDisplay {
  return {
    ...category,
    // Fallback subtitle → name nếu null/empty
    displaySubtitle: category.subtitle || category.name,
    
    // Fallback ctaText → "Xem thêm" nếu null/empty
    displayCtaText: category.ctaText || 'Xem thêm',
    
    // Route luôn có từ BE (auto-generated từ slug)
    navigationPath: category.route
  };
}

/**
 * Map từ BE response sang FE Category interface
 * BE trả globalCategoryId, FE có thể cần id field
 */
export function mapBackendCategory(backendCategory: any): Category {
  return {
    globalCategoryId: backendCategory.globalCategoryId,
    name: backendCategory.name,
    slug: backendCategory.slug,
    subtitle: backendCategory.subtitle,
    imageUrl: backendCategory.imageUrl,
    ctaText: backendCategory.ctaText,
    route: backendCategory.route,
    displayOrder: backendCategory.displayOrder,
    isActive: backendCategory.isActive,
    metaData: backendCategory.metaData
  };
}
