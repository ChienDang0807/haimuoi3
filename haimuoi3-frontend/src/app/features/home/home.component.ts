import { Component, OnInit, PLATFORM_ID, signal, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { take } from 'rxjs/operators';
import { HeaderComponent } from '../../shared/layout/header/header.component';
import { FooterComponent } from '../../shared/layout/footer/footer.component';
import { HeroSectionComponent } from './hero-section/hero-section.component';
import { CategorySectionComponent } from './category-section/category-section.component';
import { NewArrivalsComponent } from './new-arrivals/new-arrivals.component';
import { PoliciesSectionComponent } from './policies-section/policies-section.component';
import { TestimonialsComponent } from './testimonials/testimonials.component';
import { HeroService } from '../../core/services/hero.service';
import { ProductService } from '../../core/services/product.service';
import { CategoryService } from '../../core/services/category.service';
import { PolicyService } from '../../core/services/policy.service';
import { TestimonialService } from '../../core/services/testimonial.service';
import { WishlistService } from '../../core/services/wishlist.service';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/constants/user-role';
import { HeroSlide, Product, Category, Policy, Testimonial } from '../../shared/interfaces';
import { NewArrivals } from '../../core/constants';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    FooterComponent,
    HeroSectionComponent,
    CategorySectionComponent,
    NewArrivalsComponent,
    PoliciesSectionComponent,
    TestimonialsComponent,
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomeComponent implements OnInit {
  private readonly newArrivalsPageSize = NewArrivals.PAGE_SIZE;
  private newArrivalsPage = 0;
  private isLastNewArrivalsPage = false;

  private heroService = inject(HeroService);
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private policyService = inject(PolicyService);
  private testimonialService = inject(TestimonialService);
  private wishlistService = inject(WishlistService);
  private authService = inject(AuthService);
  private platformId = inject(PLATFORM_ID);

  heroSlides = signal<HeroSlide[]>([]);
  categories = signal<Category[]>([]);
  newProducts = signal<Product[]>([]);
  wishlistRecord = signal<Record<string, boolean>>({});
  isLoadingMoreNewProducts = signal(false);
  mainPolicies = signal<Policy[]>([]);
  benefitPolicies = signal<Policy[]>([]);
  testimonials = signal<Testimonial[]>([]);

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    this.loadData();
  }

  private loadData(): void {
    this.heroService.getHeroSlides().subscribe(slides => {
      this.heroSlides.set(slides);
    });

    this.categoryService.getCategories().subscribe(categories => {
      this.categories.set(categories);
    });

    this.loadInitialNewArrivals();

    this.policyService.getMainPolicies().subscribe(policies => {
      this.mainPolicies.set(policies);
    });

    this.policyService.getBenefitPolicies().subscribe(policies => {
      this.benefitPolicies.set(policies);
    });

    this.testimonialService.getTestimonials().subscribe(testimonials => {
      this.testimonials.set(testimonials);
    });
  }

  hasMoreNewProducts(): boolean {
    return !this.isLastNewArrivalsPage;
  }

  onLoadMoreNewArrivals(): void {
    if (this.isLoadingMoreNewProducts() || this.isLastNewArrivalsPage) {
      return;
    }

    this.isLoadingMoreNewProducts.set(true);
    this.productService.getNewArrivalsPage(this.newArrivalsPage, this.newArrivalsPageSize).subscribe({
      next: response => {
        const merged = [...this.newProducts(), ...response.products];
        this.newProducts.set(merged);
        this.newArrivalsPage += 1;
        this.isLastNewArrivalsPage = response.isLast;
        this.isLoadingMoreNewProducts.set(false);
        this.syncWishlistContains(merged);
      },
      error: error => {
        console.error('Failed to load more new arrivals', error);
        this.isLoadingMoreNewProducts.set(false);
      },
    });
  }

  onWishlistChanged(event: { productId: string; inWishlist: boolean }): void {
    this.wishlistRecord.update(prev => ({ ...prev, [event.productId]: event.inWishlist }));
  }

  private loadInitialNewArrivals(): void {
    this.isLoadingMoreNewProducts.set(true);
    this.productService.getNewArrivalsPage(0, this.newArrivalsPageSize).subscribe({
      next: response => {
        this.newProducts.set(response.products);
        this.newArrivalsPage = 1;
        this.isLastNewArrivalsPage = response.isLast;
        this.isLoadingMoreNewProducts.set(false);
        this.syncWishlistContains(response.products);
      },
      error: error => {
        console.error('Failed to load new arrivals from API, using fallback data', error);
        this.productService.getNewArrivals().subscribe(products => {
          const slice = products.slice(0, this.newArrivalsPageSize);
          this.newProducts.set(slice);
          this.newArrivalsPage = 1;
          this.isLastNewArrivalsPage = products.length <= this.newArrivalsPageSize;
          this.isLoadingMoreNewProducts.set(false);
          this.syncWishlistContains(slice);
        });
      },
    });
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
