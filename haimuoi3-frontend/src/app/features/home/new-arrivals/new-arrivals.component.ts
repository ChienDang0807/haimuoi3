import {
  Component,
  Input,
  Output,
  EventEmitter,
  ChangeDetectionStrategy,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Product } from '../../../shared/interfaces';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { NewArrivals } from '../../../core/constants';

@Component({
  selector: 'app-new-arrivals',
  standalone: true,
  imports: [CommonModule, ProductCardComponent],
  templateUrl: './new-arrivals.component.html',
  styleUrl: './new-arrivals.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NewArrivalsComponent implements OnChanges {
  private readonly initialVisibleCount: number = NewArrivals.INITIAL_VISIBLE_COUNT;
  private readonly loadMoreStep: number = NewArrivals.LOAD_MORE_STEP;
  private pendingVisibleCount: number = this.initialVisibleCount;

  @Input({ required: true }) products: Product[] = [];
  @Input() hasMoreFromServer = false;
  @Input() isLoadingMore = false;
  /** Map product id -> in wishlist (customer only; filled by parent). */
  @Input() wishlistRecord: Record<string, boolean> = {};
  @Output() loadMoreRequested = new EventEmitter<void>();
  @Output() wishlistChanged = new EventEmitter<{ productId: string; inWishlist: boolean }>();
  visibleCount = this.initialVisibleCount;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['products']) {
      if (changes['products'].firstChange) {
        this.visibleCount = Math.min(this.initialVisibleCount, this.products.length);
        this.pendingVisibleCount = this.initialVisibleCount;
        return;
      }
      this.visibleCount = Math.min(this.pendingVisibleCount, this.products.length);
    }
  }

  get visibleProducts(): Product[] {
    return this.products.slice(0, this.visibleCount);
  }

  get canLoadMore(): boolean {
    return this.visibleCount < this.products.length || this.hasMoreFromServer;
  }

  onLoadMore(): void {
    const targetVisibleCount = this.visibleCount + this.loadMoreStep;
    this.pendingVisibleCount = targetVisibleCount;
    this.visibleCount = Math.min(targetVisibleCount, this.products.length);

    if (targetVisibleCount > this.products.length && this.hasMoreFromServer && !this.isLoadingMore) {
      this.loadMoreRequested.emit();
    }
  }

  onProductClick(product: Product): void {
    console.log('Product clicked:', product);
  }

  onWishlistChanged(event: { productId: string; inWishlist: boolean }): void {
    this.wishlistChanged.emit(event);
  }
}
