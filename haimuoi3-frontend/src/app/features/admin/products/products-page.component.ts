import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { AdminSidebarComponent } from '../../../shared/layout/admin-sidebar/admin-sidebar.component';
import { AdminHeaderComponent } from '../../../shared/layout/admin-header/admin-header.component';
import { ShopOwnerApiService } from '../../../core/services/shop-owner-api.service';
import { ToastService } from '../../../core/services/toast.service';
import { ShopProductResponse } from '../../../shared/interfaces';
import { ShopAddProductDialogComponent, ShopAddProductDialogData } from './shop-add-product-dialog.component';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-products-page',
  standalone: true,
  imports: [CommonModule, AdminSidebarComponent, AdminHeaderComponent, MatButtonModule],
  templateUrl: './products-page.component.html',
  styleUrl: './products-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductsPageComponent {
  private shopOwnerApi = inject(ShopOwnerApiService);
  private dialog = inject(MatDialog);
  private toast = inject(ToastService);

  products = signal<ShopProductResponse[]>([]);
  isLoading = signal(true);
  loadError = signal<string | null>(null);

  constructor() {
    this.refreshList();
  }

  refreshList(): void {
    this.isLoading.set(true);
    this.loadError.set(null);
    this.shopOwnerApi
      .listMyProducts(0, 100)
      .pipe(take(1))
      .subscribe({
        next: res => {
          this.products.set(res.result?.content ?? []);
          this.isLoading.set(false);
        },
        error: err => {
          const msg =
            err?.error?.message ??
            (typeof err?.message === 'string' ? err.message : null) ??
            'Không thể tải danh sách sản phẩm (yêu cầu đăng nhập Shop Owner)';
          this.loadError.set(msg);
          this.isLoading.set(false);
        },
      });
  }

  onNewProduct(): void {
    this.shopOwnerApi
      .listMyShopCategories(0, 100)
      .pipe(take(1))
      .subscribe({
        next: cats => {
          const shopCategories = cats.result?.content ?? [];
          if (shopCategories.length === 0) {
            this.toast.error('Hãy tạo ít nhất một danh mục của cửa hàng trước khi thêm sản phẩm');
            return;
          }
          const data: ShopAddProductDialogData = { shopCategories };
          const ref = this.dialog.open(ShopAddProductDialogComponent, {
            width: '420px',
            data,
          });
          ref.afterClosed().subscribe(result => {
            if (!result) {
              return;
            }
            this.shopOwnerApi.createMyProduct(result).subscribe({
              next: () => {
                this.toast.success('Đã thêm sản phẩm mới');
                this.refreshList();
              },
              error: e => {
                const msg = e?.error?.message ?? 'Thêm sản phẩm thất bại';
                this.toast.error(msg);
              },
            });
          });
        },
        error: () => {
          this.toast.error('Không thể tải danh mục cửa hàng');
        },
      });
  }

  onToggleProductStatus(product: ShopProductResponse): void {
    this.shopOwnerApi.toggleProductStatus(product.id).subscribe({
      next: res => {
        this.toast.success(`Đã cập nhật trạng thái: ${res.result?.status}`);
        this.refreshList();
      },
      error: e => {
        const msg = e?.error?.message ?? 'Cập nhật trạng thái thất bại';
        this.toast.error(msg);
      },
    });
  }

  onEditProduct(product: ShopProductResponse): void {
    // Phase 4: Implement edit product dialog. For now, we can reuse add dialog with data if it supports it.
    // However, the spec says "Edit product dialog (name, description, price, brand, shop category, featured, badge type)"
    // Let's check if ShopAddProductDialogComponent can be used for editing.
    this.shopOwnerApi
      .listMyShopCategories(0, 100)
      .pipe(take(1))
      .subscribe({
        next: cats => {
          const shopCategories = cats.result?.content ?? [];
          const data: ShopAddProductDialogData = { shopCategories, initialData: product };
          const ref = this.dialog.open(ShopAddProductDialogComponent, {
            width: '420px',
            data,
          });
          ref.afterClosed().subscribe(result => {
            if (!result) return;
            this.shopOwnerApi.updateMyProduct(product.id, result).subscribe({
              next: () => {
                this.toast.success('Đã cập nhật sản phẩm');
                this.refreshList();
              },
              error: e => {
                this.toast.error(e?.error?.message ?? 'Cập nhật thất bại');
              },
            });
          });
        },
      });
  }

  formatPrice(p: number | string | undefined | null): string {
    if (p === undefined || p === null) {
      return '—';
    }
    const n = typeof p === 'string' ? parseFloat(p) : p;
    if (Number.isNaN(n)) {
      return '—';
    }
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n);
  }
}
