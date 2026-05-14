import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AdminSidebarComponent } from '../../../shared/layout/admin-sidebar/admin-sidebar.component';
import { AdminHeaderComponent } from '../../../shared/layout/admin-header/admin-header.component';
import { ShopOrderService } from '../../../core/services/shop-order.service';
import { OrderDetail, PageResponse } from '../../../shared/interfaces';
import { orderStatusBadgeClass, orderStatusLabel } from '../../account/account-order-status.util';

@Component({
  selector: 'app-orders-page',
  standalone: true,
  imports: [CommonModule, AdminSidebarComponent, AdminHeaderComponent],
  templateUrl: './orders-page.component.html',
  styleUrl: './orders-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrdersPageComponent {
  private readonly shopOrderService = inject(ShopOrderService);
  private readonly destroyRef = inject(DestroyRef);

  readonly pageSize = 20;
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly orderPage = signal<PageResponse<OrderDetail> | null>(null);

  readonly orders = computed(() => this.orderPage()?.content ?? []);

  readonly totalLabel = computed(() => {
    const p = this.orderPage();
    if (!p) return 'Loading…';
    return `${p.totalElements} order${p.totalElements === 1 ? '' : 's'} for your shop`;
  });

  readonly paginationLabel = computed(() => {
    const p = this.orderPage();
    if (!p || p.totalPages <= 1) return '';
    return `Page ${p.number + 1} of ${p.totalPages}`;
  });

  constructor() {
    this.loadPage(0);
  }

  loadPage(pageIndex: number): void {
    this.loading.set(true);
    this.error.set(false);
    this.shopOrderService
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
