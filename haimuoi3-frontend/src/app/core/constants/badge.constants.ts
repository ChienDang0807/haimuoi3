import { ProductBadge } from '../../shared/interfaces';

/**
 * Badge type từ backend
 */
export type BackendBadgeType = 'NEW' | 'RARE' | 'SALE' | 'NONE' | string;

/**
 * Mapping từ backend badge type sang UI badge config
 * Dễ dàng thay đổi text/color theo theme mà không ảnh hưởng business logic
 */
export const BADGE_CONFIG: Record<string, ProductBadge> = {
  NEW: { text: 'New', color: 'black' },
  RARE: { text: 'Rare', color: 'blue' },
  SALE: { text: 'Sale', color: 'red' }
} as const;

/**
 * Resolve badge config từ backend type
 * NONE hoặc unknown type => undefined (không hiển thị badge)
 */
export function resolveBadgeConfig(badgeType: BackendBadgeType | null | undefined): ProductBadge | undefined {
  if (!badgeType || badgeType === 'NONE') {
    return undefined;
  }
  return BADGE_CONFIG[badgeType];
}
