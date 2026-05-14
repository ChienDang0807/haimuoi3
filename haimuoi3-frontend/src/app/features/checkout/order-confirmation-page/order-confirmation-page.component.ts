import { ChangeDetectionStrategy, Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { HeaderComponent } from '../../../shared/layout/header/header.component';
import { FooterComponent } from '../../../shared/layout/footer/footer.component';
import { environment } from '../../../../environments/environment';
import { ApiEndpoints } from '../../../core/constants/api-endpoints';
import { ApiResponse, OrderDetail } from '../../../shared/interfaces';

type PageState = 'loading' | 'success' | 'failed' | 'polling';

@Component({
  selector: 'app-order-confirmation-page',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent, FooterComponent],
  templateUrl: './order-confirmation-page.component.html',
  styleUrl: './order-confirmation-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrderConfirmationPageComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);

  pageState = signal<PageState>('loading');
  order = signal<OrderDetail | null>(null);
  orderId = signal<number | null>(null);

  private pollInterval: ReturnType<typeof setInterval> | null = null;

  ngOnInit(): void {
    const idParam = this.route.snapshot.queryParamMap.get('orderId');
    if (!idParam) {
      this.pageState.set('failed');
      return;
    }
    const id = parseInt(idParam, 10);
    this.orderId.set(id);
    this.fetchOrderStatus(id);
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }

  private fetchOrderStatus(id: number): void {
    this.http
      .get<ApiResponse<OrderDetail>>(`${environment.apiUrl}${ApiEndpoints.CUSTOMERS_ME_ORDERS}/${id}`)
      .subscribe({
        next: (res) => {
          const orderData = res.result;
          this.order.set(orderData);
          this.resolveState(orderData.status);
        },
        error: () => {
          this.pageState.set('failed');
        },
      });
  }

  private resolveState(status: string): void {
    if (status === 'PAID' || status === 'CONFIRMED') {
      this.stopPolling();
      this.pageState.set('success');
    } else if (status === 'PAYMENT_FAILED' || status === 'CANCELLED') {
      this.stopPolling();
      this.pageState.set('failed');
    } else if (status === 'PENDING_PAYMENT') {
      this.pageState.set('polling');
      this.startPolling();
    } else {
      this.pageState.set('success');
    }
  }

  private startPolling(): void {
    if (this.pollInterval) return;
    this.pollInterval = setInterval(() => {
      const id = this.orderId();
      if (id) this.fetchOrderStatus(id);
    }, 3000);
  }

  private stopPolling(): void {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
      this.pollInterval = null;
    }
  }
}
