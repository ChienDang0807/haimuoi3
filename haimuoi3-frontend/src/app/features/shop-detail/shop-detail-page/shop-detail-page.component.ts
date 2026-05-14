import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { take } from 'rxjs/operators';
import { Shop, Product, Review, ReviewStats } from '../../../shared/interfaces';
import { ShopService } from '../../../core/services/shop.service';
import { ReviewService } from '../../../core/services/review.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserRole } from '../../../core/constants/user-role';
import { HeaderComponent } from '../../../shared/layout/header/header.component';
import { FooterComponent } from '../../../shared/layout/footer/footer.component';
import { ShopHeaderComponent } from '../shop-header/shop-header.component';
import { ProductGridComponent } from '../product-grid/product-grid.component';
import { ShopReviewsSectionComponent } from '../shop-reviews-section/shop-reviews-section.component';

@Component({
  selector: 'app-shop-detail-page',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    FooterComponent,
    ShopHeaderComponent,
    ProductGridComponent,
    ShopReviewsSectionComponent,
  ],
  templateUrl: './shop-detail-page.component.html',
  styleUrl: './shop-detail-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShopDetailPageComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private shopService = inject(ShopService);
  private reviewService = inject(ReviewService);
  private wishlistService = inject(WishlistService);
  private authService = inject(AuthService);

  shop = signal<Shop | null>(null);
  products = signal<Product[]>([]);
  wishlistRecord = signal<Record<string, boolean>>({});
  reviews = signal<Review[]>([]);
  reviewStats = signal<ReviewStats | null>(null);

  ngOnInit(): void {
    const shopId = this.route.snapshot.paramMap.get('id');

    if (shopId) {
      this.loadShopData(shopId);
    }
  }

  private loadShopData(shopId: string): void {
    this.shopService.getShop(shopId).subscribe(shop => {
      this.shop.set(shop);
    });

    this.shopService.getShopProducts(shopId).subscribe(products => {
      this.products.set(products);
      this.syncWishlistContains(products);
    });

    this.reviewService.getShopReviews(shopId).subscribe(reviews => {
      this.reviews.set(reviews);
    });

    this.reviewService.getShopReviewStats(shopId).subscribe(stats => {
      this.reviewStats.set(stats);
    });
  }

  onFollow(shopId: string): void {
    console.log('Follow shop:', shopId);
  }

  onMessage(shopId: string): void {
    console.log('Message shop:', shopId);
  }

  onWishlistChanged(event: { productId: string; inWishlist: boolean }): void {
    this.wishlistRecord.update(prev => ({ ...prev, [event.productId]: event.inWishlist }));
  }

  private syncWishlistContains(products: Product[]): void {
    const user = this.authService.currentUser();
    if (!this.authService.isLoggedIn() || user?.role !== UserRole.CUSTOMER) {
      this.wishlistRecord.set({});
      return;
    }
    const ids = products.map(p => p.id).filter(Boolean);
    if (ids.length === 0) {
      this.wishlistRecord.set({});
      return;
    }
    this.wishlistService
      .contains(ids)
      .pipe(take(1))
      .subscribe({
        next: map => this.wishlistRecord.set(map),
        error: () => this.wishlistRecord.set({}),
      });
  }
}
