import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  HostListener,
  PLATFORM_ID,
  computed,
  inject,
  signal,
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NgxEchartsDirective } from 'ngx-echarts';
import type { EChartsOption } from 'echarts';
import { AdminSidebarComponent } from '../../../shared/layout/admin-sidebar/admin-sidebar.component';
import { AdminHeaderComponent } from '../../../shared/layout/admin-header/admin-header.component';
import { AuthService } from '../../../core/services/auth.service';
import { ShopOwnerApiService } from '../../../core/services/shop-owner-api.service';
import { orderStatusBadgeClass } from '../../account/account-order-status.util';
import {
  buildDashboardChartOptions,
  formatDashboardCurrency,
  mapShopDashboardResponse,
  statusSliceColor,
} from './mappers/shop-dashboard.mapper';
import {
  DashboardOrderColumnKey,
  DashboardPageVm,
} from './models/shop-dashboard.models';
import {
  DEFAULT_DASHBOARD_ORDER_COLUMNS,
  isDashboardOrderColumnVisible,
  loadVisibleDashboardOrderColumns,
  saveVisibleDashboardOrderColumns,
  toggleDashboardOrderColumn,
} from './utils/dashboard-orders-column-preferences';

const EMPTY_DASHBOARD: DashboardPageVm = {
  header: { shopName: 'Cửa hàng của bạn', subtitle: 'Tổng quan 7 ngày gần nhất' },
  metrics: [],
  revenueTrend: [],
  revenueTrendPrevious: [],
  recentOrders: [],
  orderStatusBreakdown: [],
  fulfillmentRatePercent: 0,
  topProducts: [],
  lowStockAlerts: [],
  shopHealth: null,
};

const COLUMN_LABELS: Record<DashboardOrderColumnKey, string> = {
  orderId: 'Mã đơn',
  customer: 'Khách hàng',
  items: 'Sản phẩm',
  status: 'Trạng thái',
  time: 'Thời gian',
  amount: 'Tổng tiền',
  action: 'Thao tác',
};

@Component({
  selector: 'app-shop-dashboard',
  templateUrl: './dashboard-page.component.html',
  styleUrls: ['./dashboard-page.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule, AdminSidebarComponent, AdminHeaderComponent, NgxEchartsDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardPageComponent {
  private readonly shopOwnerApi = inject(ShopOwnerApiService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly platformId = inject(PLATFORM_ID);

  readonly isBrowser = isPlatformBrowser(this.platformId);
  readonly dateRangeLabel = '7 ngày gần nhất';
  readonly columnOptions = DEFAULT_DASHBOARD_ORDER_COLUMNS;
  readonly columnLabels = COLUMN_LABELS;

  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly noShopLinked = signal(false);
  readonly isColumnMenuOpen = signal(false);
  readonly dashboard = signal<DashboardPageVm>(EMPTY_DASHBOARD);
  readonly visibleColumns = signal<DashboardOrderColumnKey[]>([...DEFAULT_DASHBOARD_ORDER_COLUMNS]);
  readonly chartOptions = signal<EChartsOption | null>(null);

  readonly metrics = computed(() => this.dashboard().metrics);
  readonly recentOrders = computed(() => this.dashboard().recentOrders);
  readonly topProducts = computed(() => this.dashboard().topProducts);
  readonly lowStockAlerts = computed(() => this.dashboard().lowStockAlerts);
  readonly orderStatusBreakdown = computed(() => this.dashboard().orderStatusBreakdown);
  readonly fulfillmentRatePercent = computed(() => this.dashboard().fulfillmentRatePercent);
  readonly header = computed(() => this.dashboard().header);

  constructor() {
    this.visibleColumns.set(
      loadVisibleDashboardOrderColumns(this.authService.currentUser()?.userId, this.platformId),
    );
    if (this.isBrowser) {
      this.loadDashboard();
    } else {
      this.isLoading.set(false);
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target;
    if (!(target instanceof Element) || !target.closest('.column-menu-wrapper')) {
      this.isColumnMenuOpen.set(false);
    }
  }

  loadDashboard(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.noShopLinked.set(false);

    this.shopOwnerApi
      .getMyShopDashboard('7d')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: res => {
          const vm = mapShopDashboardResponse(res.result);
          this.dashboard.set(vm);
          if (this.isBrowser) {
            this.chartOptions.set(
              buildDashboardChartOptions(vm.revenueTrend, vm.revenueTrendPrevious),
            );
          }
          this.isLoading.set(false);
        },
        error: err => {
          const status = err?.status;
          if (status === 404) {
            this.noShopLinked.set(true);
            this.dashboard.set(EMPTY_DASHBOARD);
            this.chartOptions.set(null);
            this.isLoading.set(false);
            return;
          }

          const message =
            err?.error?.message ??
            (typeof err?.message === 'string' ? err.message : null) ??
            'Không thể tải dữ liệu tổng quan. Vui lòng thử lại sau.';
          this.errorMessage.set(message);
          this.isLoading.set(false);
        },
      });
  }

  toggleColumnMenu(): void {
    this.isColumnMenuOpen.update(open => !open);
  }

  isColumnVisible(column: DashboardOrderColumnKey): boolean {
    return isDashboardOrderColumnVisible(this.visibleColumns(), column);
  }

  isColumnToggleDisabled(column: DashboardOrderColumnKey): boolean {
    return column === 'orderId' || column === 'action';
  }

  onColumnToggle(column: DashboardOrderColumnKey): void {
    const next = toggleDashboardOrderColumn(this.visibleColumns(), column);
    this.visibleColumns.set(
      saveVisibleDashboardOrderColumns(
        this.authService.currentUser()?.userId,
        next,
        this.platformId,
      ),
    );
  }

  getTrendColorClass(trend: { direction: string; isPositive: boolean }): string {
    if (trend.direction === 'neutral') {
      return 'text-secondary';
    }
    return trend.isPositive ? 'text-emerald-600' : 'text-error';
  }

  getTrendIcon(direction: string): string {
    if (direction === 'up') {
      return 'trending_up';
    }
    if (direction === 'down') {
      return 'trending_down';
    }
    return 'trending_flat';
  }

  getProductTrendIndicator(trendPercent: number, direction: string): string {
    if (direction === 'down') {
      return `↓ ${trendPercent}%`;
    }
    if (direction === 'up') {
      return `↑ ${trendPercent}%`;
    }
    return `${trendPercent}%`;
  }

  statusBadgeClass(status: string): string {
    return orderStatusBadgeClass(status);
  }

  statusSliceColor(colorToken: string): string {
    return statusSliceColor(colorToken);
  }

  formatCurrency(amount: number): string {
    return formatDashboardCurrency(amount);
  }

  viewOrderDetail(orderId: number): void {
    this.router.navigate(['/admin/orders', orderId]);
  }

  navigateToInventory(): void {
    this.router.navigate(['/admin/inventory']);
  }
}
