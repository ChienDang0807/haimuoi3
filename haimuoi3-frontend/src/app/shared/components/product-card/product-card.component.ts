import {
  Component,
  Input,
  Output,
  EventEmitter,
  ChangeDetectionStrategy,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Product } from '../../interfaces';
import { CartService } from '../../../core/services/cart.service';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { UserRole } from '../../../core/constants/user-role';
import { Router } from '@angular/router';
import { take } from 'rxjs/operators';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductCardComponent {
  private cartService = inject(CartService);
  private toastService = inject(ToastService);
  private authService = inject(AuthService);
  private wishlistService = inject(WishlistService);
  private router = inject(Router);

  @Input({ required: true }) product!: Product;
  @Input() inWishlist = false;
  @Output() productClick = new EventEmitter<Product>();
  @Output() wishlistChanged = new EventEmitter<{ productId: string; inWishlist: boolean }>();

  readonly showWishlistHeart = () =>
    this.authService.isLoggedIn() && this.authService.currentUser()?.role === UserRole.CUSTOMER;

  onCardClick(): void {
    this.productClick.emit(this.product);
  }

  onAddToCart(event: Event): void {
    event.preventDefault();
    event.stopPropagation();

    this.cartService
      .addItem({
        productId: this.product.id,
        quantity: 1,
        unitPriceSnapshot: this.product.price,
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

  onWishlistClick(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    if (!this.authService.isLoggedIn()) {
      void this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }
    if (this.authService.currentUser()?.role !== UserRole.CUSTOMER) {
      this.toastService.error('Wishlist is only available for customer accounts');
      return;
    }
    const productId = this.product.id;
    if (this.inWishlist) {
      this.wishlistService
        .remove(productId)
        .pipe(take(1))
        .subscribe({
          next: () => {
            this.toastService.success('Removed from wishlist');
            this.wishlistChanged.emit({ productId, inWishlist: false });
          },
          error: () => this.toastService.error('Could not update wishlist'),
        });
    } else {
      this.wishlistService
        .add(productId)
        .pipe(take(1))
        .subscribe({
          next: () => {
            this.toastService.success('Saved to wishlist');
            this.wishlistChanged.emit({ productId, inWishlist: true });
          },
          error: () => this.toastService.error('Could not update wishlist'),
        });
    }
  }

  getBadgeClass(color?: string): string {
    const baseClasses =
      'absolute top-4 z-10 px-2 py-1 text-[9px] font-bold tracking-widest text-white uppercase';

    switch (color) {
      case 'black':
        return `${baseClasses} left-4 bg-black`;
      case 'blue':
        return `${baseClasses} left-4 bg-blue-600`;
      case 'red':
        return `${baseClasses} left-4 bg-red-500`;
      case 'green':
        return `${baseClasses} left-4 bg-green-600`;
      default:
        return `${baseClasses} left-4 bg-black`;
    }
  }
}
