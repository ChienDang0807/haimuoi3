import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ShopOwnerApiService } from '../../../core/services/shop-owner-api.service';
import { AdminSidebarComponent } from '../../../shared/layout/admin-sidebar/admin-sidebar.component';
import { AdminHeaderComponent } from '../../../shared/layout/admin-header/admin-header.component';
import { OrderDetail } from '../../../shared/interfaces';
import { orderStatusBadgeClass, orderStatusLabel } from '../../account/account-order-status.util';
import { finalize } from 'rxjs';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-admin-order-detail-page',
  standalone: true,
  imports: [CommonModule, RouterModule, AdminSidebarComponent, AdminHeaderComponent],
  templateUrl: './admin-order-detail-page.component.html',
  styleUrl: './admin-order-detail-page.component.scss',
})
export class AdminOrderDetailPageComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private shopOwnerApi = inject(ShopOwnerApiService);
  private toastService = inject(ToastService);

  order = signal<OrderDetail | null>(null);
  isLoading = signal(true);
  isUpdating = signal(false);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadOrder(id);
    }
  }

  loadOrder(id: string | number): void {
    this.isLoading.set(true);
    this.error.set(null);
    this.shopOwnerApi.getMyShopOrderById(id)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (res) => {
          if (res.result) {
            this.order.set(res.result);
          }
        },
        error: (err) => {
          console.error('Error loading order:', err);
          this.error.set('Không tìm thấy đơn hàng hoặc có lỗi xảy ra.');
        }
      });
  }

  updateStatus(status: string): void {
    const currentOrder = this.order();
    if (!currentOrder || this.isUpdating()) return;

    this.isUpdating.set(true);
    this.shopOwnerApi.updateMyShopOrderStatus(currentOrder.id, status)
      .pipe(finalize(() => this.isUpdating.set(false)))
      .subscribe({
        next: (res) => {
          if (res.result) {
            this.order.set(res.result);
            this.toastService.success('Cập nhật trạng thái thành công');
          }
        },
        error: (err) => {
          console.error('Error updating status:', err);
          this.toastService.error(err.error?.message || 'Cập nhật thất bại');
        }
      });
  }

  get canConfirm(): boolean {
    const o = this.order();
    if (!o) return false;
    // CONFIRMED + COD -> READY_TO_SHIP
    // PAID + STRIPE -> READY_TO_SHIP
    return (o.status === 'CONFIRMED' && o.paymentMethod === 'COD') || 
           (o.status === 'PAID' && o.paymentMethod === 'STRIPE');
  }

  get canShip(): boolean {
    const o = this.order();
    return o?.status === 'READY_TO_SHIP';
  }

  statusLabel(status: string): string {
    return orderStatusLabel(status);
  }

  statusBadgeClass(status: string): string {
    return orderStatusBadgeClass(status);
  }
}
