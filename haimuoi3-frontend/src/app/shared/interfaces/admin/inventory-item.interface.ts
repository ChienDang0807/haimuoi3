/** Matches backend InventoryItemResponse DTO */
export interface InventoryItemDto {
  productId: string;
  displayName: string;
  sku: string;
  quantityOnHand: number;
  lowStock: boolean;
}

/** @deprecated Use InventoryItemDto instead */
export interface InventoryItem {
  sku: string;
  name: string;
  description: string;
  category: string;
  stockLevel: number;
  stockStatus: 'Stable' | 'Low Stock' | 'Critical';
  unitPrice: number;
  imageUrl?: string;
}
