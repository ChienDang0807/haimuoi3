import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HeaderComponent } from '../../../shared/layout/header/header.component';
import { AccountSidebarComponent } from '../account-sidebar/account-sidebar.component';
import { WishlistService } from '../../../core/services/wishlist.service';
import { CartService } from '../../../core/services/cart.service';
import { ProductService } from '../../../core/services/product.service';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserRole } from '../../../core/constants/user-role';
import { PageResponse, ProductBuyerAvailability, WishlistItemResponse } from '../../../shared/interfaces';
import { catchError, switchMap, take } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-account-wishlist-page',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent, AccountSidebarComponent],
  templateUrl: './account-wishlist-page.component.html',
  styleUrl: './account-wishlist-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountWishlistPageComponent {
  private readonly wishlistService = inject(WishlistService);
  private readonly cartService = inject(CartService);
  private readonly productService = inject(ProductService);
  private readonly toastService = inject(ToastService);
  private readonly router = inject(Router);
  readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly pageSize = 12;
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly wishlistPage = signal<PageResponse<WishlistItemResponse> | null>(null);
  readonly currentPage = signal(0);

  readonly items = computed(() => this.wishlistPage()?.content ?? []);
  readonly isShopOwner = computed(() => this.authService.currentUser()?.role === UserRole.SHOP_OWNER);

  constructor() {
    this.loadPage(0);
  }

  loadPage(pageIndex: number): void {
    if (this.isShopOwner()) {
      this.loading.set(false);
      this.error.set(false);
      this.wishlistPage.set(null);
      return;
    }
    this.loading.set(true);
    this.error.set(false);
    this.wishlistService
      .listPaged(pageIndex, this.pageSize)
      .pipe(take(1), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: res => {
          this.wishlistPage.set(res.result ?? null);
          this.currentPage.set(pageIndex);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.error.set(true);
        },
      });
  }

  goToProduct(productId: string): void {
    void this.router.navigate(['/product', productId]);
  }

  removeItem(item: WishlistItemResponse, event: Event): void {
    event.stopPropagation();
    this.wishlistService
      .remove(item.productId)
      .pipe(take(1), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toastService.success('Removed from wishlist');
          this.loadPage(this.currentPage());
        },
        error: () => this.toastService.error('Could not remove item'),
      });
  }

  addToCartOrNavigate(item: WishlistItemResponse, event: Event): void {
    event.stopPropagation();
    if (item.availability !== 'AVAILABLE') {
      this.toastService.error('This product is not available to add to cart');
      return;
    }
    if (item.activeSkuCount !== 1) {
      void this.router.navigate(['/product', item.productId]);
      return;
    }
    this.productService
      .getProductById(item.productId)
      .pipe(
        switchMap(product =>
          this.cartService.addItem({
            productId: product.cartProductId ?? product.id,
            quantity: 1,
            unitPriceSnapshot: product.price,
          }),
        ),
        catchError(() => {
          this.toastService.error('Could not add to cart');
          return of(null);
        }),
        take(1),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(result => {
        if (result) {
          this.toastService.success('Added to cart');
        }
      });
  }

  isUnavailable(item: WishlistItemResponse): boolean {
    return item.availability !== 'AVAILABLE';
  }

  availabilityLabel(availability: ProductBuyerAvailability): string {
    switch (availability) {
      case 'OUT_OF_STOCK':
        return 'Out of stock';
      case 'DISCONTINUED':
        return 'Unavailable';
      default:
        return '';
    }
  }

  toNumber(value: number | string | null | undefined): number | null {
    if (value == null) {
      return null;
    }
    const n = typeof value === 'number' ? value : Number(value);
    return Number.isFinite(n) ? n : null;
  }
}
