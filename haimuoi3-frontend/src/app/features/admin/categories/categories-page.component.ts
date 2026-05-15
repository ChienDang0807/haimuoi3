import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { AdminHeaderComponent } from '../../../shared/layout/admin-header/admin-header.component';
import { AdminSidebarComponent } from '../../../shared/layout/admin-sidebar/admin-sidebar.component';
import { ShopOwnerApiService } from '../../../core/services/shop-owner-api.service';
import { ToastService } from '../../../core/services/toast.service';
import { ShopCategoryDto } from '../../../shared/interfaces';
import { ShopCreateCategoryDialogComponent, ShopCategoryDialogData } from './shop-create-category-dialog.component';
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
  private shopOwnerApi = inject(ShopOwnerApiService);
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
    this.shopOwnerApi
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
            'Không thể tải danh mục (yêu cầu đăng nhập Shop Owner)';
          this.loadError.set(msg);
          this.isLoading.set(false);
        },
      });
  }

  openAddCategory(): void {
    const ref = this.dialog.open(ShopCreateCategoryDialogComponent, { width: '420px' });
    ref.afterClosed().subscribe(payload => {
      if (!payload) return;
      this.shopOwnerApi.createShopCategory(payload).subscribe({
        next: () => {
          this.toast.success('Đã tạo danh mục mới');
          this.refreshList();
        },
        error: e => {
          this.toast.error(e?.error?.message ?? 'Tạo danh mục thất bại');
        },
      });
    });
  }

  onEditCategory(cat: ShopCategoryDto): void {
    const data: ShopCategoryDialogData = { initialData: cat };
    const ref = this.dialog.open(ShopCreateCategoryDialogComponent, { width: '420px', data });
    ref.afterClosed().subscribe(payload => {
      if (!payload) return;
      this.shopOwnerApi.updateShopCategory(cat.shopCategoryId, payload).subscribe({
        next: () => {
          this.toast.success('Đã cập nhật danh mục');
          this.refreshList();
        },
        error: e => {
          this.toast.error(e?.error?.message ?? 'Cập nhật thất bại');
        },
      });
    });
  }

  onToggleActive(cat: ShopCategoryDto): void {
    this.shopOwnerApi.toggleShopCategoryActive(cat.shopCategoryId).subscribe({
      next: res => {
        this.toast.success(`Đã cập nhật trạng thái: ${res.result?.active ? 'Hiển thị' : 'Tạm ẩn'}`);
        this.refreshList();
      },
      error: e => {
        this.toast.error(e?.error?.message ?? 'Cập nhật trạng thái thất bại');
      },
    });
  }

  onDeleteCategory(cat: ShopCategoryDto): void {
    if (!confirm(`Bạn có chắc chắn muốn xóa danh mục "${cat.name}"?`)) return;

    this.shopOwnerApi.deleteShopCategory(cat.shopCategoryId).subscribe({
      next: () => {
        this.toast.success('Đã xóa danh mục');
        this.refreshList();
      },
      error: e => {
        // 409 error will be handled here if category has products
        const msg = e?.status === 409 || e?.status === 500
          ? (e?.error?.message || 'Không thể xóa danh mục vì vẫn còn sản phẩm liên kết.')
          : 'Xóa danh mục thất bại';
        this.toast.error(msg);
      },
    });
  }

  statusBadgeClass(active: boolean): string {
    if (active) {
      return 'px-3 py-1 bg-emerald-100 text-[10px] font-black text-emerald-700 uppercase tracking-widest rounded-full shadow-sm';
    }
    return 'px-3 py-1 bg-slate-100 text-[10px] font-black text-slate-400 uppercase tracking-widest rounded-full shadow-sm';
  }

  statusLabel(active: boolean): string {
    return active ? 'Hiển thị' : 'Tạm ẩn';
  }

  trackById = (_: number, item: ShopCategoryDto) => item.shopCategoryId;
}
