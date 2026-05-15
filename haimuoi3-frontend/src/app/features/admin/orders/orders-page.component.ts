import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AdminSidebarComponent } from '../../../shared/layout/admin-sidebar/admin-sidebar.component';
import { AdminHeaderComponent } from '../../../shared/layout/admin-header/admin-header.component';
import { OrderDetail, PageResponse } from '../../../shared/interfaces';
import { orderStatusBadgeClass, orderStatusLabel } from '../../account/account-order-status.util';
import { ShopOwnerApiService } from '../../../core/services/shop-owner-api.service';

@Component({
  selector: 'app-orders-page',
  standalone: true,
  imports: [CommonModule, RouterModule, AdminSidebarComponent, AdminHeaderComponent],
  templateUrl: './orders-page.component.html',
  styleUrl: './orders-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrdersPageComponent {
  private readonly shopOwnerApi = inject(ShopOwnerApiService);
  private readonly destroyRef = inject(DestroyRef);

  readonly pageSize = 20;
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly orderPage = signal<PageResponse<OrderDetail> | null>(null);

  readonly orders = computed(() => this.orderPage()?.content ?? []);

    readonly totalLabel = computed(() => {
    const p = this.orderPage();
    if (!p) return 'Đang tải…';
    return `${p.totalElements} đơn hàng cho cửa hàng của bạn`;
  });

  readonly paginationLabel = computed(() => {
    const p = this.orderPage();
    if (!p || p.totalPages <= 1) return '';
    return `Trang ${p.number + 1} trên ${p.totalPages}`;
  });

  constructor() {
    this.loadPage(0);
  }

  loadPage(pageIndex: number): void {
    this.loading.set(true);
    this.error.set(false);
    this.shopOwnerApi
      .listMyShopOrders(pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.orderPage.set(res.result ?? null);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.error.set(true);
        },
      });
  }

  goPrev(): void {
    const p = this.orderPage();
    if (p && !p.first) this.loadPage(p.number - 1);
  }

  goNext(): void {
    const p = this.orderPage();
    if (p && !p.last) this.loadPage(p.number + 1);
  }

  statusLabel(status: string): string {
    return orderStatusLabel(status);
  }

  statusBadgeClass(status: string): string {
    return orderStatusBadgeClass(status);
  }
}
