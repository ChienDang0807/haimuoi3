export interface ShopDashboardHeaderVm {
  shopName: string;
  subtitle: string;
}

export interface DashboardKpiCardVm {
  label: string;
  value: string;
  icon: string;
  trend?: {
    direction: 'up' | 'down' | 'neutral';
    value: string;
    isPositive: boolean;
  };
}

export interface DashboardRevenuePointVm {
  label: string;
  revenue: number;
  orderCount?: number;
}

export interface DashboardRecentOrderVm {
  orderId: number;
  displayOrderCode: string;
  customerName: string;
  customerInitials: string;
  customerColor: string;
  itemCount: number;
  status: string;
  statusLabel: string;
  timeAgoLabel: string;
  totalAmount: number;
}

export interface DashboardOrderStatusSliceVm {
  statusKey: string;
  label: string;
  count: number;
  colorToken: string;
}

export interface DashboardTopProductVm {
  productId: string;
  name: string;
  imageUrl: string;
  salesCount: number;
  trendPercent: number;
  trendDirection: 'up' | 'down' | 'neutral';
}

export interface DashboardLowStockAlertVm {
  productId: string;
  productName: string;
  quantityOnHand: number;
}

export interface DashboardShopHealthVm {
  connectionLabel: string;
  lastSyncedAt: string;
}

export type DashboardOrderColumnKey =
  | 'orderId'
  | 'customer'
  | 'items'
  | 'status'
  | 'time'
  | 'amount'
  | 'action';

export interface DashboardPageVm {
  header: ShopDashboardHeaderVm;
  metrics: DashboardKpiCardVm[];
  revenueTrend: DashboardRevenuePointVm[];
  revenueTrendPrevious: DashboardRevenuePointVm[];
  recentOrders: DashboardRecentOrderVm[];
  orderStatusBreakdown: DashboardOrderStatusSliceVm[];
  fulfillmentRatePercent: number;
  topProducts: DashboardTopProductVm[];
  lowStockAlerts: DashboardLowStockAlertVm[];
  shopHealth: DashboardShopHealthVm | null;
}
