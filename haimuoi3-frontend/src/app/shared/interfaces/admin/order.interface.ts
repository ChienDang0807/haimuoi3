export interface Order {
  id: string;
  date: string;
  customerName: string;
  customerInitials?: string;
  amount: number;
  paymentMethod: string;
  status: 'Pending' | 'Shipped' | 'Delivered' | 'Cancelled';
}

export interface Transaction {
  id: string;
  customerName: string;
  status: string;
  amount: number;
}
