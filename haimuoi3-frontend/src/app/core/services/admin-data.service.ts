/**
 * Platform ADMIN dashboard client (`/api/v1/admin/dashboard/**`, role ADMIN).
 * Do not use from shop-owner `/admin` screens — use `ShopOwnerApiService` for `/v1/shops/my-shop/**`.
 */
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  AdminStat, 
  InventoryItem, 
  Order, 
  AdminProduct, 
  Transaction
} from '../../shared/interfaces/admin';
import { ApiResponse, PageResponse } from '../../shared/interfaces';
import { environment } from '../../../environments/environment';

export interface AdminTrendPoint {
  label: string;
  revenue: number;
  orderCount: number;
}

export interface AdminDashboardMetrics {
  metrics: AdminStat[];
  revenueTrend: AdminTrendPoint[];
}

interface BackendAdminMetric {
  label: string;
  value: string;
  icon: string;
  trendDirection: 'up' | 'down' | 'neutral';
  trendValue: string;
}

interface BackendAdminTrendPoint {
  label: string;
  revenue: number;
  orderCount: number;
}

interface BackendAdminDashboardMetrics {
  metrics: BackendAdminMetric[];
  revenueTrend: BackendAdminTrendPoint[];
}

export interface AdminModerationProduct {
  id: string;
  name: string;
  price: number;
  status: string;
  shopId: string;
  imageUrl: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class AdminDataService {
  private readonly http = inject(HttpClient);
  private readonly adminDashboardUrl = `${environment.apiUrl}/v1/admin/dashboard`;

  getDashboardMetrics(): Observable<AdminDashboardMetrics> {
    return this.http
      .get<ApiResponse<BackendAdminDashboardMetrics>>(`${this.adminDashboardUrl}/metrics`)
      .pipe(
        map(response => ({
          metrics: response.result.metrics.map(metric => ({
            label: metric.label,
            value: metric.value,
            icon: metric.icon,
            trend: {
              direction: metric.trendDirection,
              value: metric.trendValue,
            },
          })),
          revenueTrend: response.result.revenueTrend.map(point => ({
            label: point.label,
            revenue: Number(point.revenue ?? 0),
            orderCount: point.orderCount,
          })),
        }))
      );
  }

  searchModerationProducts(query = '', status = '', page = 0, size = 10): Observable<PageResponse<AdminModerationProduct>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);
    if (query.trim()) {
      params = params.set('q', query.trim());
    }
    if (status.trim()) {
      params = params.set('status', status.trim());
    }
    return this.http
      .get<ApiResponse<PageResponse<AdminModerationProduct>>>(
        `${this.adminDashboardUrl}/moderation/products`,
        { params }
      )
      .pipe(map(response => response.result));
  }

  getDashboardStats(): Observable<AdminStat[]> {
    return of([
      {
        label: 'Total Revenue',
        value: '$142,850.40',
        icon: 'insights',
        trend: { direction: 'up', value: '+12.4%' }
      },
      {
        label: 'Active Orders',
        value: 1204,
        icon: 'shopping_cart',
        trend: { direction: 'up', value: '+8.2%' }
      },
      {
        label: 'Inventory Health',
        value: '84%',
        icon: 'inventory',
        trend: { direction: 'neutral', value: 'Stable' }
      },
      {
        label: 'Customer Satisfaction',
        value: '4.9',
        icon: 'sentiment_satisfied',
        trend: { direction: 'up', value: '+0.2' }
      }
    ]);
  }

  getRecentTransactions(): Observable<Transaction[]> {
    return of([
      {
        id: 'TX-8829-01',
        customerName: 'Julian Sterling',
        status: 'Verified',
        amount: 1240.00
      },
      {
        id: 'TX-8830-02',
        customerName: 'Sarah Chen',
        status: 'Pending',
        amount: 842.50
      },
      {
        id: 'TX-8831-03',
        customerName: 'Marcus Rodriguez',
        status: 'Verified',
        amount: 1520.00
      }
    ]);
  }

  getInventoryItems(): Observable<InventoryItem[]> {
    return of([
      {
        sku: 'TITAN-X1-PRO',
        name: 'Graphite External Core',
        description: 'Premium graphite core component',
        category: 'Hardware',
        stockLevel: 1240,
        stockStatus: 'Stable',
        unitPrice: 299.00,
        imageUrl: ''
      },
      {
        sku: 'TITAN-X2-ELITE',
        name: 'Titanium Frame Module',
        description: 'Precision-engineered frame',
        category: 'Components',
        stockLevel: 85,
        stockStatus: 'Low Stock',
        unitPrice: 450.00,
        imageUrl: ''
      },
      {
        sku: 'GRAPH-PRO-2024',
        name: 'Graphite Edition Watch',
        description: 'Series 4 / Titanium Case',
        category: 'Wearables',
        stockLevel: 420,
        stockStatus: 'Stable',
        unitPrice: 1299.00,
        imageUrl: ''
      },
      {
        sku: 'TITAN-LITE-01',
        name: 'Lightweight Alloy Panel',
        description: 'Ultra-light composite panel',
        category: 'Materials',
        stockLevel: 12,
        stockStatus: 'Critical',
        unitPrice: 180.00,
        imageUrl: ''
      }
    ]);
  }

  getOrders(): Observable<Order[]> {
    return of([
      {
        id: 'ORD-7721',
        date: 'Oct 12, 2023',
        customerName: 'Julian Harrison',
        customerInitials: 'JH',
        amount: 1240.00,
        paymentMethod: 'Credit Card',
        status: 'Pending'
      },
      {
        id: 'ORD-7722',
        date: 'Oct 13, 2023',
        customerName: 'Sarah Mitchell',
        customerInitials: 'SM',
        amount: 890.50,
        paymentMethod: 'PayPal',
        status: 'Shipped'
      },
      {
        id: 'ORD-7723',
        date: 'Oct 14, 2023',
        customerName: 'David Chen',
        customerInitials: 'DC',
        amount: 2150.00,
        paymentMethod: 'Credit Card',
        status: 'Delivered'
      },
      {
        id: 'ORD-7724',
        date: 'Oct 15, 2023',
        customerName: 'Emily Rodriguez',
        customerInitials: 'ER',
        amount: 450.00,
        paymentMethod: 'Debit Card',
        status: 'Pending'
      }
    ]);
  }

  getProducts(): Observable<AdminProduct[]> {
    return of([
      {
        id: 'prod-001',
        name: 'Graphite Edition Watch',
        description: 'Series 4 / Titanium Case',
        sku: 'SKU-GR4-2024',
        category: 'Wearables',
        price: 1299.00,
        status: 'In Stock',
        imageUrl: ''
      },
      {
        id: 'prod-002',
        name: 'Titanium Pro Headphones',
        description: 'Noise-cancelling / Premium audio',
        sku: 'SKU-TH-2024',
        category: 'Audio',
        price: 399.00,
        status: 'In Stock',
        imageUrl: ''
      },
      {
        id: 'prod-003',
        name: 'Graphite Laptop Stand',
        description: 'Adjustable / Ergonomic design',
        sku: 'SKU-LS-2024',
        category: 'Accessories',
        price: 149.00,
        status: 'Low Stock',
        imageUrl: ''
      },
      {
        id: 'prod-004',
        name: 'Titanium Mechanical Keyboard',
        description: 'RGB / Hot-swappable switches',
        sku: 'SKU-KB-2024',
        category: 'Peripherals',
        price: 279.00,
        status: 'Out of Stock',
        imageUrl: ''
      }
    ]);
  }

  getOrderSummaryStats(): Observable<AdminStat[]> {
    return of([
      { label: 'Total Orders', value: 1284, icon: 'list_alt', trend: { direction: 'up', value: '+12%' } },
      { label: 'Pending', value: 42, icon: 'pending' },
      { label: 'Completed', value: 1192, icon: 'check_circle' },
      { label: 'Revenue', value: '$142.8k', icon: 'payments' }
    ]);
  }

  getProductStats(): Observable<AdminStat[]> {
    return of([
      { label: 'Inventory Value', value: '$2,410,800.00', trend: { direction: 'up', value: '+12%' } },
      { label: 'Stock Utilization', value: '94.2%' },
      { label: 'Categories Active', value: 18 }
    ]);
  }
}
