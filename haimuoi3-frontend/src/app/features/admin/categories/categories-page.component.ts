import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { AdminHeaderComponent } from '../../../shared/layout/admin-header/admin-header.component';
import { AdminSidebarComponent } from '../../../shared/layout/admin-sidebar/admin-sidebar.component';
import { MyShopService } from '../../../core/services/my-shop.service';
import { ToastService } from '../../../core/services/toast.service';
import { ShopCategoryDto } from '../../../shared/interfaces';
import { ShopCreateCategoryDialogComponent } from './shop-create-category-dialog.component';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-categories-page',
  standalone: true,
  imports: [CommonModule, AdminSidebarComponent, AdminHeaderComponent, MatButtonModule],
  templateUrl: './categories-page.component.html',
  styleUrl: './categories-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoriesPageComponent {
  private myShop = inject(MyShopService);
  private dialog = inject(MatDialog);
  private toast = inject(ToastService);

  categories = signal<ShopCategoryDto[]>([]);
  isLoading = signal(true);
  loadError = signal<string | null>(null);

  constructor() {
    this.refreshList();
  }

  refreshList(): void {
    this.isLoading.set(true);
    this.loadError.set(null);
    this.myShop
      .listMyShopCategories(0, 100)
      .pipe(take(1))
      .subscribe({
        next: res => {
          this.categories.set(res.result?.content ?? []);
          this.isLoading.set(false);
        },
        error: err => {
          const msg =
            err?.error?.message ??
            (typeof err?.message === 'string' ? err.message : null) ??
            'Could not load shop categories (need SHOP_OWNER login)';
          this.loadError.set(msg);
          this.isLoading.set(false);
        },
      });
  }

  openAddCategory(): void {
    const ref = this.dialog.open(ShopCreateCategoryDialogComponent, { width: '420px' });
    ref.afterClosed().subscribe(payload => {
      if (!payload) {
        return;
      }
      this.myShop.createShopCategory(payload).subscribe({
        next: () => {
          this.toast.success('Shop category created');
          this.refreshList();
        },
        error: e => {
          const msg = e?.error?.message ?? 'Failed to create shop category';
          this.toast.error(msg);
        },
      });
    });
  }

  statusBadgeClass(active: boolean): string {
    if (active) {
      return 'px-2 py-1 bg-green-100 text-[10px] font-black text-green-700 uppercase rounded';
    }
    return 'px-2 py-1 bg-surface-container-highest text-[10px] font-black text-on-surface-variant uppercase rounded';
  }

  statusLabel(active: boolean): string {
    return active ? 'Active' : 'Inactive';
  }

  trackById = (_: number, item: ShopCategoryDto) => item.shopCategoryId;
}
