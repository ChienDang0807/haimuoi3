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
import { MyShopService } from '../../../core/services/my-shop.service';
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
  private myShop = inject(MyShopService);
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
    this.myShop
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
            'Could not load products (need SHOP_OWNER login)';
          this.loadError.set(msg);
          this.isLoading.set(false);
        },
      });
  }

  onNewProduct(): void {
    this.myShop
      .listMyShopCategories(0, 100)
      .pipe(take(1))
      .subscribe({
        next: cats => {
          const shopCategories = cats.result?.content ?? [];
          if (shopCategories.length === 0) {
            this.toast.error('Create at least one shop category before adding a product');
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
            this.myShop.createMyProduct(result).subscribe({
              next: () => {
                this.toast.success('Product created');
                this.refreshList();
              },
              error: e => {
                const msg = e?.error?.message ?? 'Failed to create product';
                this.toast.error(msg);
              },
            });
          });
        },
        error: () => {
          this.toast.error('Could not load shop categories for form');
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
    return new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' }).format(n);
  }
}
