import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HeaderComponent } from '../../../shared/layout/header/header.component';
import { AccountSidebarComponent } from '../account-sidebar/account-sidebar.component';
import { CustomerOrderService } from '../../../core/services/customer-order.service';
import { OrderDetail, PageResponse } from '../../../shared/interfaces';
import {
  isOrderActiveShipment,
  isOrderCompleted,
  orderStatusBadgeClass,
  orderStatusLabel,
} from '../account-order-status.util';

@Component({
  selector: 'app-account-orders-page',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent, AccountSidebarComponent],
  templateUrl: './account-orders-page.component.html',
  styleUrl: './account-orders-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountOrdersPageComponent {
  private readonly customerOrderService = inject(CustomerOrderService);
  private readonly destroyRef = inject(DestroyRef);

  readonly pageSize = 20;
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly orderPage = signal<PageResponse<OrderDetail> | null>(null);
  readonly currentPage = signal(0);

  readonly orders = computed(() => this.orderPage()?.content ?? []);

  readonly totalOrders = computed(() => this.orderPage()?.totalElements ?? 0);

  readonly activeOnPageCount = computed(() =>
    this.orders().filter((o) => isOrderActiveShipment(o.status)).length,
  );

  readonly completedThisMonthOnPage = computed(() => {
    const now = new Date();
    return this.orders().filter((o) => {
      if (!isOrderCompleted(o.status) || !o.createdAt) return false;
      const d = new Date(o.createdAt);
      return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear();
    }).length;
  });

  readonly inTransitOnPage = computed(() =>
    this.orders().filter((o) => o.status === 'SHIPPING').length,
  );

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
    this.customerOrderService
      .listMyOrders(pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.orderPage.set(res.result ?? null);
          this.currentPage.set(pageIndex);
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
