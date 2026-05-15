import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AdminSidebarComponent } from '../../../shared/layout/admin-sidebar/admin-sidebar.component';
import { AdminHeaderComponent } from '../../../shared/layout/admin-header/admin-header.component';
import { InventoryItemDto, PageResponse } from '../../../shared/interfaces';
import { ShopOwnerApiService } from '../../../core/services/shop-owner-api.service';

/** Frontend low stock threshold for display purposes */
const LOW_STOCK_THRESHOLD = 10;

@Component({
  selector: 'app-inventory-page',
  standalone: true,
  imports: [CommonModule, FormsModule, AdminSidebarComponent, AdminHeaderComponent],
  templateUrl: './inventory-page.component.html',
  styleUrl: './inventory-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InventoryPageComponent {
  private readonly shopOwnerApi = inject(ShopOwnerApiService);
  private readonly destroyRef = inject(DestroyRef);

  readonly pageSize = 20;
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly inventoryPage = signal<PageResponse<InventoryItemDto> | null>(null);

  readonly items = computed(() => this.inventoryPage()?.content ?? []);

  readonly totalLabel = computed(() => {
    const p = this.inventoryPage();
    if (!p) return 'Đang tải…';
    return `${p.totalElements} sản phẩm trong kho`;
  });

  readonly paginationLabel = computed(() => {
    const p = this.inventoryPage();
    if (!p || p.totalPages <= 1) return '';
    return `Trang ${p.number + 1} trên ${p.totalPages}`;
  });

  // Adjust stock dialog state
  readonly dialogOpen = signal(false);
  readonly dialogItem = signal<InventoryItemDto | null>(null);
  readonly dialogQuantity = signal<number>(0);
  readonly dialogSaving = signal(false);
  readonly toastMessage = signal<string | null>(null);
  readonly toastType = signal<'success' | 'error'>('success');

  constructor() {
    this.loadPage(0);
  }

  loadPage(pageIndex: number): void {
    this.loading.set(true);
    this.error.set(false);
    this.shopOwnerApi
      .listMyShopInventory(pageIndex, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.inventoryPage.set(res.result ?? null);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.error.set(true);
        },
      });
  }

  goPrev(): void {
    const p = this.inventoryPage();
    if (p && !p.first) this.loadPage(p.number - 1);
  }

  goNext(): void {
    const p = this.inventoryPage();
    if (p && !p.last) this.loadPage(p.number + 1);
  }

  openAdjustDialog(item: InventoryItemDto): void {
    this.dialogItem.set(item);
    this.dialogQuantity.set(item.quantityOnHand);
    this.dialogOpen.set(true);
  }

  closeDialog(): void {
    this.dialogOpen.set(false);
    this.dialogItem.set(null);
    this.dialogSaving.set(false);
  }

  submitAdjust(): void {
    const item = this.dialogItem();
    if (!item) return;

    this.dialogSaving.set(true);
    this.shopOwnerApi
      .adjustMyShopInventory(item.productId, this.dialogQuantity())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.dialogSaving.set(false);
          this.closeDialog();
          this.showToast('Cập nhật tồn kho thành công', 'success');
          // Refresh current page
          const p = this.inventoryPage();
          this.loadPage(p?.number ?? 0);
        },
        error: () => {
          this.dialogSaving.set(false);
          this.showToast('Cập nhật tồn kho thất bại', 'error');
        },
      });
  }

  private showToast(message: string, type: 'success' | 'error'): void {
    this.toastMessage.set(message);
    this.toastType.set(type);
    setTimeout(() => this.toastMessage.set(null), 3000);
  }

  isLowStock(item: InventoryItemDto): boolean {
    return item.lowStock || item.quantityOnHand <= LOW_STOCK_THRESHOLD;
  }
}
