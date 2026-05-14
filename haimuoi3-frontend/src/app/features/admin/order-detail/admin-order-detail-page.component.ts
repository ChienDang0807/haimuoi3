import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ShopOrderService } from '../../../core/services/shop-order.service';
import { AdminSidebarComponent } from '../../../shared/layout/admin-sidebar/admin-sidebar.component';
import { AdminHeaderComponent } from '../../../shared/layout/admin-header/admin-header.component';
import { OrderDetail } from '../../../shared/interfaces';
import { orderStatusBadgeClass, orderStatusLabel } from '../../account/account-order-status.util';

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
  private shopOrderService = inject(ShopOrderService);

  order = this.shopOrderService.getOrderById(this.route.snapshot.paramMap.get('id')!);

  ngOnInit(): void {
    // Order data is fetched via resolver or service in template
  }

  statusLabel(status: string): string {
    return orderStatusLabel(status);
  }

  statusBadgeClass(status: string): string {
    return orderStatusBadgeClass(status);
  }
}
