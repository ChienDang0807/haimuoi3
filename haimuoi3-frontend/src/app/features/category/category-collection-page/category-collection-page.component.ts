import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, ParamMap, Router, RouterModule } from '@angular/router';
import { combineLatest, of } from 'rxjs';
import { catchError, distinctUntilChanged, map, switchMap, take, tap } from 'rxjs/operators';
import { HeaderComponent } from '../../../shared/layout/header/header.component';
import { FooterComponent } from '../../../shared/layout/footer/footer.component';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { Category, Product } from '../../../shared/interfaces';
import { CategoryService } from '../../../core/services/category.service';
import { ProductService } from '../../../core/services/product.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserRole } from '../../../core/constants/user-role';

const PAGE_SIZE = 32;

interface CategoryCatalogFilters {
  globalCategoryId: string | null;
  q: string;
  minPrice: string;
  maxPrice: string;
  minRating: string;
}

function emptyFilters(): CategoryCatalogFilters {
  return {
    globalCategoryId: null,
    q: '',
    minPrice: '',
    maxPrice: '',
    minRating: '',
  };
}

function routeFingerprint(pm: ParamMap, qm: ParamMap): string {
  const slug = pm.get('slug') ?? 'all';
  const keys = ['globalCategoryId', 'q', 'minPrice', 'maxPrice', 'minRating', 'categoryName'];
  const parts = keys.map(k => `${k}=${qm.get(k) ?? ''}`);
  return `${slug}|${parts.join('&')}`;
}

@Component({
  selector: 'app-category-collection-page',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent, FooterComponent, ProductCardComponent],
  templateUrl: './category-collection-page.component.html',
  styleUrl: './category-collection-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoryCollectionPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly categoryService = inject(CategoryService);
  private readonly productService = inject(ProductService);
  private readonly wishlistService = inject(WishlistService);
  private readonly authService = inject(AuthService);

  readonly categories = signal<Category[]>([]);
  readonly products = signal<Product[]>([]);
  readonly wishlistRecord = signal<Record<string, boolean>>({});
  readonly routeSlug = signal<string>('all');
  readonly draft = signal<CategoryCatalogFilters>(emptyFilters());
  readonly applied = signal<CategoryCatalogFilters>(emptyFilters());
  readonly isLoading = signal(false);
  readonly loadError = signal<string | null>(null);

  readonly currentCategoryName = computed(() => {
    const filters = this.applied();
    if (filters.globalCategoryId) {
      return (
        this.categories().find(c => c.globalCategoryId === filters.globalCategoryId)?.name ?? 'Collections'
      );
    }
    const slug = this.routeSlug();
    if (slug !== 'all') {
      return this.categories().find(c => c.slug === slug)?.name ?? 'Collections';
    }
    return 'Collections';
  });

  constructor() {
    this.categoryService
      .getCategories()
      .pipe(
        tap(categories => this.categories.set(categories)),
        switchMap(categories =>
          combineLatest([this.route.paramMap, this.route.queryParamMap]).pipe(
            map(([pm, qm]) => ({ pm, qm, categories })),
            distinctUntilChanged((a, b) => routeFingerprint(a.pm, a.qm) === routeFingerprint(b.pm, b.qm)),
            tap(({ pm, qm, categories: cats }) => {
              const slug = pm.get('slug') ?? 'all';
              this.routeSlug.set(slug);
              const parsed = this.parseFiltersFromUrl(slug, qm, cats);
              this.draft.set({ ...parsed });
              this.applied.set({ ...parsed });
              this.loadError.set(null);
              this.isLoading.set(true);
            }),
            switchMap(({ pm, qm, categories: cats }) => {
              const slug = pm.get('slug') ?? 'all';
              const parsed = this.parseFiltersFromUrl(slug, qm, cats);
              let minPrice = this.parseOptionalNumber(parsed.minPrice);
              let maxPrice = this.parseOptionalNumber(parsed.maxPrice);
              if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
                [minPrice, maxPrice] = [maxPrice, minPrice];
              }
              return this.productService
                .getGlobalProductsPage({
                  page: 0,
                  size: PAGE_SIZE,
                  q: parsed.q.trim() || null,
                  globalCategoryId: parsed.globalCategoryId,
                  minPrice,
                  maxPrice,
                  minRating: this.parseOptionalNumber(parsed.minRating),
                })
                .pipe(
                  catchError(() => {
                    this.products.set([]);
                    this.isLoading.set(false);
                    this.loadError.set('Không tải được danh sách sản phẩm. Thử lại sau.');
                    this.syncWishlistContains([]);
                    return of(null);
                  }),
                );
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(page => {
        if (page == null) {
          return;
        }
        this.products.set(page.products);
        this.isLoading.set(false);
        this.loadError.set(null);
        this.syncWishlistContains(page.products);
      });
  }

  patchDraft(partial: Partial<CategoryCatalogFilters>): void {
    this.draft.update(d => ({ ...d, ...partial }));
  }

  onCategorySelect(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.patchDraft({ globalCategoryId: value ? value : null });
  }

  onDraftQ(event: Event): void {
    this.patchDraft({ q: (event.target as HTMLInputElement).value });
  }

  onDraftMinPrice(event: Event): void {
    this.patchDraft({ minPrice: (event.target as HTMLInputElement).value });
  }

  onDraftMaxPrice(event: Event): void {
    this.patchDraft({ maxPrice: (event.target as HTMLInputElement).value });
  }

  onDraftMinRating(event: Event): void {
    this.patchDraft({ minRating: (event.target as HTMLInputElement).value });
  }

  onSearch(): void {
    const d = this.draft();
    const slug = d.globalCategoryId
      ? this.categories().find(c => c.globalCategoryId === d.globalCategoryId)?.slug ?? 'all'
      : 'all';
    const queryParams: Record<string, string> = {};
    if (d.globalCategoryId) {
      queryParams['globalCategoryId'] = d.globalCategoryId;
    }
    const qTrim = d.q.trim();
    if (qTrim) {
      queryParams['q'] = qTrim;
    }
    const minP = d.minPrice.trim();
    if (minP) {
      queryParams['minPrice'] = minP;
    }
    const maxP = d.maxPrice.trim();
    if (maxP) {
      queryParams['maxPrice'] = maxP;
    }
    const minR = d.minRating.trim();
    if (minR) {
      queryParams['minRating'] = minR;
    }
    const cat = this.categories().find(c => c.globalCategoryId === d.globalCategoryId);
    if (cat?.name) {
      queryParams['categoryName'] = cat.name;
    }
    void this.router.navigate(['/category', slug], { queryParams, queryParamsHandling: '' });
  }

  onWishlistChanged(event: { productId: string; inWishlist: boolean }): void {
    this.wishlistRecord.update(prev => ({ ...prev, [event.productId]: event.inWishlist }));
  }

  private parseFiltersFromUrl(slug: string, qm: ParamMap, categories: Category[]): CategoryCatalogFilters {
    let globalCategoryId = qm.get('globalCategoryId')?.trim() || null;
    if (!globalCategoryId && slug !== 'all') {
      globalCategoryId = categories.find(c => c.slug === slug)?.globalCategoryId ?? null;
    }
    return {
      globalCategoryId,
      q: qm.get('q') ?? '',
      minPrice: qm.get('minPrice') ?? '',
      maxPrice: qm.get('maxPrice') ?? '',
      minRating: qm.get('minRating') ?? '',
    };
  }

  private parseOptionalNumber(raw: string): number | null | undefined {
    const t = raw.trim();
    if (!t) {
      return undefined;
    }
    const n = Number(t);
    return Number.isFinite(n) ? n : undefined;
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
