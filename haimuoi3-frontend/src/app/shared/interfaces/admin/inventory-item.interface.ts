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
