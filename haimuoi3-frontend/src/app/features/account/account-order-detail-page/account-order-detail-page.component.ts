import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, take } from 'rxjs';
import { HeaderComponent } from '../../../shared/layout/header/header.component';
import { AccountSidebarComponent } from '../account-sidebar/account-sidebar.component';
import { CustomerOrderService } from '../../../core/services/customer-order.service';
import { OrderDetail } from '../../../shared/interfaces';
import {
  canCustomerCancelOrder,
  canCustomerConfirmDelivered,
  orderStatusBadgeClass,
  orderStatusLabel,
} from '../account-order-status.util';

@Component({
  selector: 'app-account-order-detail-page',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent, AccountSidebarComponent],
  templateUrl: './account-order-detail-page.component.html',
  styleUrl: './account-order-detail-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountOrderDetailPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly customerOrderService = inject(CustomerOrderService);
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = signal(true);
  readonly error = signal(false);
  readonly order = signal<OrderDetail | null>(null);
  readonly actionBusy = signal(false);
  private orderId = NaN;

  constructor() {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = idParam ? parseInt(idParam, 10) : NaN;
    this.orderId = id;
    if (Number.isNaN(id)) {
      this.loading.set(false);
      this.error.set(true);
      return;
    }
    this.fetchOrder(id);
  }

  canCancel(): boolean {
    const o = this.order();
    return !!o && canCustomerCancelOrder(o.status);
  }

  canConfirmDelivered(): boolean {
    const o = this.order();
    return !!o && canCustomerConfirmDelivered(o.status);
  }

  cancelOrder(): void {
    if (Number.isNaN(this.orderId) || !this.canCancel() || this.actionBusy()) return;
    this.actionBusy.set(true);
    this.customerOrderService
      .cancelMyOrder(this.orderId)
      .pipe(take(1), finalize(() => this.actionBusy.set(false)))
      .subscribe({
        next: (res) => this.order.set(res.result ?? null),
        error: () => {},
      });
  }

  confirmDelivered(): void {
    if (Number.isNaN(this.orderId) || !this.canConfirmDelivered() || this.actionBusy()) return;
    this.actionBusy.set(true);
    this.customerOrderService
      .confirmDeliveredMyOrder(this.orderId)
      .pipe(take(1), finalize(() => this.actionBusy.set(false)))
      .subscribe({
        next: (res) => this.order.set(res.result ?? null),
        error: () => {},
      });
  }

  private fetchOrder(id: number): void {
    this.loading.set(true);
    this.customerOrderService
      .getMyOrder(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.order.set(res.result ?? null);
          this.loading.set(false);
          this.error.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.error.set(true);
        },
      });
  }

  statusLabel(status: string): string {
    return orderStatusLabel(status);
  }

  statusBadgeClass(status: string): string {
    return orderStatusBadgeClass(status);
  }
}
