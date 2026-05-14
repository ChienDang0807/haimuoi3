export interface AdminProduct {
  id: string;
  name: string;
  description: string;
  sku: string;
  category: string;
  price: number;
  status: 'In Stock' | 'Low Stock' | 'Out of Stock';
  imageUrl: string;
}
