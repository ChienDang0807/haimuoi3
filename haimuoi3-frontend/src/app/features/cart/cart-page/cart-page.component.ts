import { ChangeDetectionStrategy, Component, signal, inject, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { HeaderComponent } from '../../../shared/layout/header/header.component';
import { FooterComponent } from '../../../shared/layout/footer/footer.component';
import { CartService } from '../../../core/services/cart.service';
import { ProductService } from '../../../core/services/product.service';
import { AuthService } from '../../../core/services/auth.service';
import { Product } from '../../../shared/interfaces';

interface CartItemDisplay {
  productId: string;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  badge?: {
    text: string;
    icon?: string;
  };
  metaLine?: {
    text: string;
    icon?: string;
  };
  quantity: number;
}

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [CommonModule, RouterLink, HeaderComponent, FooterComponent],
  templateUrl: './cart-page.component.html',
  styleUrl: './cart-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CartPageComponent implements OnInit {
  private cartService = inject(CartService);
  private productService = inject(ProductService);
  private authService = inject(AuthService);
  private router = inject(Router);

  readonly cart = this.cartService.cart;
  readonly isLoading = this.cartService.isLoading;
  
  private productsCache = signal<Map<string, Product>>(new Map());
  
  readonly items = computed(() => {
    const cartItems = this.cart()?.items ?? [];
    const cache = this.productsCache();
    
    return cartItems.map(item => {
      const product = cache.get(item.productId);
      return {
        productId: item.productId,
        name: product?.name ?? 'Loading...',
        description: product?.description ?? '',
        price: item.unitPriceSnapshot,
        imageUrl: product?.imageUrl ?? '',
        quantity: item.quantity
      } as CartItemDisplay;
    });
  });

  ngOnInit(): void {
    this.cartService.loadCart().subscribe(cart => {
      if (cart?.items) {
        this.loadProductDetails(cart.items.map(i => i.productId));
      }
    });
  }

  readonly coupon = signal<string>('');

  readonly recentlyViewed = signal([
    {
      id: 'rv-1',
      name: 'Winter Parka 01',
      price: 520,
      imageUrl:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuB6PdGbGxWPbfJwjdA-JPQKWTahKgBykeXfh75Gw36w7WVsFcK1OablHieghNloFXLSh2ddGQlEX5zxVVynRlBmDl7UKG0dfgmOZZob8GaQlWbhpvOrJaOLGifZ7N5W6KSMy_pEHX8T66_XMfuZOD_B6S_3srlJbNpRXvTxfWgnlh1usX-fvO2SKVFaBJ9S2K8H4fc3GLNt6WKN8MxaOb0iRcK0uLcg5fR3UL04Fv0gjex8840gVJT_cZeKPiaqr6V-BMU4DIOub4M'
    },
    {
      id: 'rv-2',
      name: 'Studio Collection',
      subtitle: 'Explore Apparel',
      imageUrl:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuC4npnF0yzMJOBJbxSdc-n-RuKOQ-VYC-yeJFdAiIwaov-h0eGSpCbkB34lQ1pNZ4JEB1dvWE9OUWmtytUG2rU1vpJZYhH4WEJTPwlz7GLTUrEolKEJ5Yy5cMNdiBiXC08HTgPKBMklzJtNAEzuofUKzK2MgF83B8dDTdeXu_QnLDIU4Sfzx4THAfVwR97imOBFZhPqZ_Da-0JMKOPzPqb9CiqNHRWVZO-pbecjDQH4aXK9ZOTutmDmjn05lpHUYWmRxdBEDcqOcTY'
    }
  ]);

  readonly recommendations = signal([
    {
      id: 'rec-1',
      label: 'Staff Pick',
      name: 'Performance Trainer 04',
      price: 195,
      imageUrl:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuCLFlFjv1c-7EJ592GC_h-2QWBrKt1AYpz2JVS69RQGcsgORsaBM9jpfJe0P0I0M70UeXxMue7Qw2n7QHZtcmvogj2Xl9ty24dQWPSTwQYnjjzzpsNEjhuaimGqU1WjLN_g0r5yl__EC0nuIuSRtQGBYeiralBvX9sG0GytpAtknArRt_JzZF8IDO-n35dOkt9Z5TkCW3KLEc1RcIz0e9OG33PIr8L0-L0SvP07nBfILM-I5kVBJMm8eMs-nfYiF3fL9nf79lEUT8c'
    },
    {
      id: 'rec-2',
      label: 'New Color',
      name: 'Slate Commuter Tote',
      price: 320,
      imageUrl:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuDRan3yWSak65VL5w1xiG4DGWJTvWVEVU0Dm9VON-PjmY-tTaUTqfh-JQnEavoGSyRwl53Y68AvshOYEE1yHighsxVIazyQ2s7VR58TNA17n08niUNKWvqjUVTRIFpNRx4Sa874sL1tRg_2GCyvB1td-Wcar2FvgqejBPUtuv7wALRv5Obf9VJ3Me1K5OK7UFvqmnN2ZC48sU8VUFEudjrU8KyLp5g4nM4qzDSOVQEIjpeSjd6hCYg3U8BIleP9WX1p7ePScOmswqQ'
    },
    {
      id: 'rec-3',
      label: 'Limited',
      name: 'Graphite Chronograph',
      price: 850,
      imageUrl:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuD1iVKClZMWoZwtN6gtq9maFAjZ5G92DSX1amu2_zd2DNuKxlU58vu9zf8uU2HeHb5sNcW5AiK5JyRgggxSRLKg25ETyMSvzIwJOAoXC6j3YzgE5qsQJLhBqB1mcQNsxfEb2Z-jq6uZfluHhbiSZNKE9dPusTC8qBE2yXXJABo_Qr6-4TjlyZi-QbtE6mcreWDZjTX53zPqHeLC1NwoNCG6AIkD-ZPiSjdrIoa8R5jA2HkD__NWuSzKxtPcHOCdAl5Nv6M4tnNi8qo'
    }
  ]);

  subtotal(): number {
    return this.items().reduce((sum, item) => sum + item.price * item.quantity, 0);
  }

  itemQuantityTotal(): number {
    return this.items().reduce((sum, item) => sum + item.quantity, 0);
  }

  total(): number {
    return this.subtotal();
  }

  formatMoney(value: number): string {
    return `$${value.toFixed(2)}`;
  }

  private loadProductDetails(productIds: string[]): void {
    const cache = this.productsCache();
    const missingIds = Array.from(new Set(productIds.filter(id => !cache.has(id))));
    if (missingIds.length === 0) {
      return;
    }

    this.productService.getProductsByIds(missingIds).subscribe(products => {
      this.productsCache.update(currentCache => {
        const newCache = new Map(currentCache);
        products.forEach(product => {
          newCache.set(product.id, product);
        });
        return newCache;
      });
    });
  }

  decrement(productId: string): void {
    const currentItem = this.cart()?.items.find(i => i.productId === productId);
    if (!currentItem) return;
    
    const newQuantity = Math.max(1, currentItem.quantity - 1);
    this.cartService.updateItemQuantityOptimistic(productId, newQuantity);
  }

  increment(productId: string): void {
    const currentItem = this.cart()?.items.find(i => i.productId === productId);
    if (!currentItem) return;
    
    this.cartService.updateItemQuantityOptimistic(productId, currentItem.quantity + 1);
  }

  remove(productId: string): void {
    this.cartService.removeItem(productId).subscribe();
  }

  onCheckout(): void {
    if (this.items().length === 0) {
      return;
    }
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/checkout' } });
      return;
    }
    this.router.navigate(['/checkout']);
  }
}

