import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  HostListener,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { catchError, debounceTime, distinctUntilChanged, filter, switchMap, take } from 'rxjs/operators';
import { of, Subject } from 'rxjs';
import { CartService } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';
import { ProductService } from '../../../core/services/product.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { ToastService } from '../../../core/services/toast.service';
import { UserRole } from '../../../core/constants/user-role';
import { Product, WishlistItemResponse } from '../../../shared/interfaces';
import { NotificationBellComponent } from '../../../shared/components/notification-bell/notification-bell.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, NotificationBellComponent],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeaderComponent implements OnInit {
  private cartService = inject(CartService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);
  private productService = inject(ProductService);
  private wishlistService = inject(WishlistService);
  private toastService = inject(ToastService);
  readonly authService = inject(AuthService);
  private readonly searchInput$ = new Subject<string>();

  readonly cartCount = this.cartService.totalItems;
  hasNotifications = signal<boolean>(true);
  searchQuery = signal<string>('');
  searchSuggestions = signal<Product[]>([]);
  isSuggestLoading = signal<boolean>(false);
  readonly isAccountMenuOpen = signal(false);
  readonly isWishlistDropdownOpen = signal(false);
  readonly wishlistRecentItems = signal<WishlistItemResponse[]>([]);
  readonly wishlistRecentLoading = signal(false);
  readonly isShopOwner = computed(
    () => this.authService.currentUser()?.role === UserRole.SHOP_OWNER,
  );
  readonly isCustomer = computed(
    () => this.authService.isLoggedIn() && this.authService.currentUser()?.role === UserRole.CUSTOMER,
  );

  ngOnInit(): void {
    this.cartService.loadCart().subscribe();
    this.router.events
      .pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.closeAccountMenu();
        this.closeWishlistDropdown();
      });

    this.searchInput$
      .pipe(
        debounceTime(250),
        distinctUntilChanged(),
        switchMap(query => {
          const trimmed = query.trim();
          if (!trimmed) {
            this.isSuggestLoading.set(false);
            return of([]);
          }
          this.isSuggestLoading.set(true);
          return this.productService.suggestProducts(trimmed).pipe(catchError(() => of([])));
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(suggestions => {
        this.searchSuggestions.set(suggestions);
        this.isSuggestLoading.set(false);
      });
  }

  onSearch(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.searchQuery.set(target.value);
    this.searchInput$.next(target.value);
  }

  onSearchSubmit(): void {
    const first = this.searchSuggestions()[0];
    if (first) {
      this.openSuggestion(first);
    }
  }

  openSuggestion(product: Product): void {
    this.searchSuggestions.set([]);
    this.searchQuery.set('');
    this.router.navigate(['/product', product.id]);
  }

  onCategoryClick(): void {
    this.router.navigate(['/category/all']);
  }

  onGuestAccountClick(): void {
    this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
  }

  onAccountButtonClick(event: MouseEvent): void {
    event.stopPropagation();
    this.closeWishlistDropdown();
    this.isAccountMenuOpen.update(v => !v);
  }

  closeAccountMenu(): void {
    this.isAccountMenuOpen.set(false);
  }

  onLogoutFromMenu(): void {
    this.closeAccountMenu();
    this.closeWishlistDropdown();
    this.authService.logout();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const el = event.target as HTMLElement;
    if (this.isAccountMenuOpen()) {
      if (!el.closest('.account-menu-root')) {
        this.closeAccountMenu();
      }
    }
    if (this.isWishlistDropdownOpen()) {
      if (!el.closest('.wishlist-menu-root')) {
        this.closeWishlistDropdown();
      }
    }
  }

  onWishlistButtonClick(event: MouseEvent): void {
    event.stopPropagation();
    this.closeAccountMenu();
    const open = !this.isWishlistDropdownOpen();
    this.isWishlistDropdownOpen.set(open);
    if (open && this.isCustomer()) {
      this.loadWishlistRecent();
    }
  }

  closeWishlistDropdown(): void {
    this.isWishlistDropdownOpen.set(false);
  }

  private loadWishlistRecent(): void {
    this.wishlistRecentLoading.set(true);
    this.wishlistService
      .listRecent(3)
      .pipe(take(1), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: res => {
          this.wishlistRecentItems.set(res.result ?? []);
          this.wishlistRecentLoading.set(false);
        },
        error: () => {
          this.wishlistRecentItems.set([]);
          this.wishlistRecentLoading.set(false);
        },
      });
  }

  onActionClick(action: string): void {
    if (action === 'shipping' && this.authService.isLoggedIn()) {
      this.router.navigate(['/account/orders']);
      return;
    }
    if (action === 'wishlist') {
      return;
    }
  }

  goToLoginForWishlist(): void {
    this.closeWishlistDropdown();
    this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
  }

  viewAllWishlist(): void {
    this.closeWishlistDropdown();
    void this.router.navigate(['/account/wishlist']);
  }

  openWishlistProduct(productId: string): void {
    this.closeWishlistDropdown();
    void this.router.navigate(['/product', productId]);
  }

  removeWishlistItem(productId: string, event: Event): void {
    event.stopPropagation();
    this.wishlistService
      .remove(productId)
      .pipe(take(1), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.loadWishlistRecent(),
        error: () => {},
      });
  }

  addWishlistItemToCart(item: WishlistItemResponse, event: Event): void {
    event.stopPropagation();
    if (item.availability !== 'AVAILABLE') {
      return;
    }
    if (item.activeSkuCount !== 1) {
      void this.router.navigate(['/product', item.productId]);
      this.closeWishlistDropdown();
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
        take(1),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.toastService.success('Added to cart');
          this.closeWishlistDropdown();
        },
        error: () => this.toastService.error('Could not add to cart'),
      });
  }

  wishlistPrice(item: WishlistItemResponse): string {
    const min = item.minPrice != null ? Number(item.minPrice) : null;
    const max = item.maxPrice != null ? Number(item.maxPrice) : null;
    if (min == null || !Number.isFinite(min)) {
      return '';
    }
    if (max != null && Number.isFinite(max) && max !== min) {
      return `$${min.toFixed(2)} – $${max.toFixed(2)}`;
    }
    return `$${min.toFixed(2)}`;
  }
}
