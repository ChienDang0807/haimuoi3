import { Component, input, output, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Product } from '../../../shared/interfaces';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';

type FilterType = 'all' | 'best-picks' | 'limited';

@Component({
  selector: 'app-product-grid',
  standalone: true,
  imports: [CommonModule, ProductCardComponent],
  templateUrl: './product-grid.component.html',
  styleUrl: './product-grid.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductGridComponent {
  products = input.required<Product[]>();
  wishlistRecord = input<Record<string, boolean>>({});
  wishlistChanged = output<{ productId: string; inWishlist: boolean }>();

  activeFilter = signal<FilterType>('all');

  setFilter(filter: FilterType): void {
    this.activeFilter.set(filter);
  }

  get filteredProducts(): Product[] {
    const filter = this.activeFilter();
    const allProducts = this.products();

    if (filter === 'all') {
      return allProducts;
    }

    if (filter === 'best-picks') {
      return allProducts.filter(p => p.badge?.text.toLowerCase().includes('best'));
    }

    if (filter === 'limited') {
      return allProducts.filter(p => p.badge?.text.toLowerCase().includes('limited'));
    }

    return allProducts;
  }
}
