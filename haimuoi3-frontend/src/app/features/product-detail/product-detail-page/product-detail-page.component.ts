import { Component, inject, signal, ChangeDetectionStrategy, DestroyRef, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, distinctUntilChanged, filter, map, switchMap, take } from 'rxjs/operators';
import { forkJoin, of, EMPTY } from 'rxjs';
import { Product, Shop, Review, ReviewStats, BreadcrumbItem, ConfigOption } from '../../../shared/interfaces';
import { ProductService } from '../../../core/services/product.service';
import { ReviewService } from '../../../core/services/review.service';
import { ShopService } from '../../../core/services/shop.service';
import { CartService } from '../../../core/services/cart.service';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { UserRole } from '../../../core/constants/user-role';
import { CartSessionKeys } from '../../../core/constants';
import { HeaderComponent } from '../../../shared/layout/header/header.component';
import { FooterComponent } from '../../../shared/layout/footer/footer.component';
import { BreadcrumbComponent } from '../../../shared/components/breadcrumb/breadcrumb.component';
import { ProductGalleryComponent } from '../product-gallery/product-gallery.component';
import { ProductInfoComponent } from '../product-info/product-info.component';
import { TechnicalSpecsComponent } from '../technical-specs/technical-specs.component';
import { ShopInfoCardComponent } from '../shop-info-card/shop-info-card.component';
import { ProductReviewsSectionComponent } from '../product-reviews-section/product-reviews-section.component';

@Component({
  selector: 'app-product-detail-page',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    FooterComponent,
    BreadcrumbComponent,
    ProductGalleryComponent,
    ProductInfoComponent,
    TechnicalSpecsComponent,
    ShopInfoCardComponent,
    ProductReviewsSectionComponent,
  ],
  templateUrl: './product-detail-page.component.html',
  styleUrl: './product-detail-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductDetailPageComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);
  private productService = inject(ProductService);
  private reviewService = inject(ReviewService);
  private shopService = inject(ShopService);
  private cartService = inject(CartService);
  private toastService = inject(ToastService);
  private wishlistService = inject(WishlistService);
  private platformId = inject(PLATFORM_ID);

  readonly authService = inject(AuthService);
  readonly UserRole = UserRole;

  product = signal<Product | null>(null);
  shop = signal<Shop | null>(null);
  reviews = signal<Review[]>([]);
  reviewStats = signal<ReviewStats | null>(null);
  breadcrumbs = signal<BreadcrumbItem[]>([
    { label: 'Home', url: '/' },
    { label: 'Collections', url: '/category/all' },
  ]);
  errorMessage = signal<string | null>(null);
  inWishlist = signal(false);

  constructor() {
    this.route.paramMap
      .pipe(
        map(pm => pm.get('id')),
        filter((id): id is string => !!id && id.length > 0),
        distinctUntilChanged(),
        switchMap(productId => {
          this.resetState();
          return this.productService.getProductById(productId).pipe(
            switchMap(product =>
              forkJoin({
                stats: this.reviewService.getProductReviewStats(productId),
                reviews: this.reviewService.getProductReviews(productId, 0, 20),
                shop: product.shopId ? this.shopService.getShopById(product.shopId) : of(null),
              }).pipe(
                map(({ stats, reviews, shop }) => {
                  const merged: Product = {
                    ...product,
                    rating: stats.averageRating,
                    reviewCount: stats.totalReviews,
                  };
                  return { product: merged, stats, reviews, shop };
                }),
              ),
            ),
            catchError(err => {
              const msg =
                err?.error?.message ??
                (typeof err?.message === 'string' ? err.message : null) ??
                'Could not load product';
              this.errorMessage.set(msg);
              return EMPTY;
            }),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ product, stats, reviews, shop }) => {
        this.product.set(product);
        this.reviewStats.set(stats);
        this.reviews.set(reviews);
        this.shop.set(shop);
        this.breadcrumbs.set(this.buildBreadcrumbs(product));
        this.loadWishlistState(product.id);
      });
  }

  private resetState(): void {
    this.errorMessage.set(null);
    this.product.set(null);
    this.shop.set(null);
    this.reviews.set([]);
    this.reviewStats.set(null);
    this.inWishlist.set(false);
    this.breadcrumbs.set([
      { label: 'Home', url: '/' },
      { label: 'Collections', url: '/category/all' },
    ]);
  }

  private loadWishlistState(productId: string): void {
    this.inWishlist.set(false);
    if (!this.authService.isLoggedIn() || this.authService.currentUser()?.role !== UserRole.CUSTOMER) {
      return;
    }
    this.wishlistService
      .contains([productId])
      .pipe(take(1))
      .subscribe({
        next: map => this.inWishlist.set(!!map[productId]),
        error: () => this.inWishlist.set(false),
      });
  }

  onWishlistToggle(): void {
    const current = this.product();
    if (!current || this.authService.currentUser()?.role !== UserRole.CUSTOMER) {
      return;
    }
    const productId = current.id;
    if (this.inWishlist()) {
      this.wishlistService
        .remove(productId)
        .pipe(take(1))
        .subscribe({
          next: () => {
            this.inWishlist.set(false);
            this.toastService.success('Removed from wishlist');
          },
          error: () => this.toastService.error('Could not update wishlist'),
        });
    } else {
      this.wishlistService
        .add(productId)
        .pipe(take(1))
        .subscribe({
          next: () => {
            this.inWishlist.set(true);
            this.toastService.success('Saved to wishlist');
          },
          error: () => this.toastService.error('Could not update wishlist'),
        });
    }
  }

  private buildBreadcrumbs(product: Product): BreadcrumbItem[] {
    const slug = product.category?.trim();
    const label = product.categoryLabel?.trim() || slug || 'Collections';
    const midUrl = slug ? `/category/${encodeURIComponent(slug)}` : '/category/all';
    const midLabel = slug ? label : 'Collections';
    return [
      { label: 'Home', url: '/' },
      { label: midLabel, url: midUrl },
      { label: product.name, active: true },
    ];
  }

  onBuyNow(): void {
    const currentProduct = this.product();
    if (!currentProduct) {
      return;
    }
    const productId = currentProduct.cartProductId ?? currentProduct.id;
    const unitPriceSnapshot = currentProduct.price;

    if (!this.authService.isLoggedIn()) {
      if (isPlatformBrowser(this.platformId)) {
        sessionStorage.setItem(
          CartSessionKeys.PENDING_BUY_NOW,
          JSON.stringify({
            productId,
            quantity: 1,
            unitPriceSnapshot,
          }),
        );
      }
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/checkout' } });
      return;
    }

    this.cartService
      .addItem({
        productId,
        quantity: 1,
        unitPriceSnapshot,
      })
      .subscribe({
        next: () => {
          this.router.navigate(['/checkout']);
        },
        error: err => {
          console.error('Buy now: failed to add to cart', err);
          this.toastService.error('Could not proceed to checkout. Please try again.');
        },
      });
  }

  onAddToCart(): void {
    const currentProduct = this.product();
    if (!currentProduct) return;

    const productId = currentProduct.cartProductId ?? currentProduct.id;

    this.cartService
      .addItem({
        productId,
        quantity: 1,
        unitPriceSnapshot: currentProduct.price,
      })
      .subscribe({
        next: () => {
          this.toastService.success('Added to cart');
        },
        error: err => {
          console.error('Failed to add to cart', err);
          this.toastService.error('Failed to add item to cart');
        },
      });
  }

  onConfigChange(option: ConfigOption): void {
    const current = this.product();
    if (!current?.skus?.length) {
      return;
    }
    const sku = current.skus.find(s => s.id === option.id);
    if (!sku) {
      return;
    }
    this.product.set({
      ...current,
      price: sku.price,
      cartProductId: sku.id,
      configurations: current.configurations?.map(c => ({
        ...c,
        selected: c.id === option.id,
      })),
    });
  }

  onVisitStore(shopKey: string): void {
    this.router.navigate(['/shop', shopKey]);
  }
}
