import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  ApiResponse,
  PageResponse,
  ProductReviewResponseDto,
  ProductReviewStatsDto,
  RatingDistribution,
  Review,
  ReviewStats,
} from '../../shared/interfaces';
import { environment } from '../../../environments/environment';
import { ApiEndpoints } from '../constants';

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private http = inject(HttpClient);
  private readonly apiRoot = environment.apiUrl;

  getProductReviews(productId: string, page = 0, size = 20): Observable<Review[]> {
    const base = `${this.apiRoot}${ApiEndpoints.PRODUCTS}/${encodeURIComponent(productId)}`;
    const url = `${base}${ApiEndpoints.PRODUCTS_REVIEWS}?page=${page}&size=${size}`;
    return this.http.get<ApiResponse<PageResponse<ProductReviewResponseDto>>>(url).pipe(
      map(res => (res.result?.content ?? []).map(r => this.mapProductReview(r))),
      catchError(() => of([])),
    );
  }

  getProductReviewStats(productId: string): Observable<ReviewStats> {
    const base = `${this.apiRoot}${ApiEndpoints.PRODUCTS}/${encodeURIComponent(productId)}`;
    const url = `${base}${ApiEndpoints.PRODUCTS_REVIEWS_STATS}`;
    return this.http.get<ApiResponse<ProductReviewStatsDto>>(url).pipe(
      map(res => this.mapProductReviewStats(res.result)),
      catchError(() => of(this.emptyProductReviewStats())),
    );
  }

  getShopReviews(shopId: string): Observable<Review[]> {
    const reviews: Review[] = [
      {
        id: '1',
        userName: 'Marcus V.',
        userAvatar:
          'https://lh3.googleusercontent.com/aida-public/AB6AXuDPwxXSvr8JSdncb4c_gZDJ6bW3MfX3IzEQ7qugWgSe_U8upXsW6wxcvQ51Zb6hi1qtNIx6XwRl0UwxN_FparEqS2r-YZV2AssBtPTHPYp-PeaW7DtwTb-lkM6JN9xbqRFADQ59NqQlP5EVvoepjarRNnoDyT75mQdIFmTsWcr-HXdCAbE3nUv40LlevPFCS_Z6oA1shPuJoiysUajGwUn6_s-Bt_S7cheXHbGAK7wmSNAEolhcDPuOvsHyIOJFOheyseahHsj_AfY',
        rating: 5,
        comment:
          'The build quality of the Graphite Chrono is simply unparalleled. It feels solid, weighted exactly as it should, and the matte finish is beautiful in person. The shipping was fast and the packaging was museum-grade.',
        date: 'October 14, 2023',
      },
      {
        id: '2',
        userName: 'Elena G.',
        userAvatar:
          'https://lh3.googleusercontent.com/aida-public/AB6AXuBMb4OgbQgk4MDHbZR4tJoC_BTO0DLT31pOk6Fn9G7L5QYplmExuMJq6QSJ-xIIOp9YFveC7I0eTooJ3zUx83C31tctEDnq0fm0gV2LN-dLqv44aJ0iMueu4krvwE0B-89QtskXzGaGkX7O01iodAe1ISusc9uTubQyXUMFEubKmKWLEg821L8IzeewC-AGyYLEYikfbfv5kxIrk4eEiBUalLrMMyDCGXOJJe22OEbKjZzHwTkw6lTXRqiu6JyaUJcah3DYYytGgs8',
        rating: 5,
        comment:
          'Finally, a shop that understands minimalist utility. The tech pouch has replaced three other organizers I was using. Clean, efficient, and looks incredible on my desk.',
        date: 'September 28, 2023',
      },
    ];

    return of(reviews);
  }

  getShopReviewStats(_shopId: string): Observable<ReviewStats> {
    const stats: ReviewStats = {
      averageRating: 4.9,
      totalReviews: 148,
      distribution: {
        5: 136,
        4: 9,
        3: 3,
        2: 0,
        1: 0,
      },
    };

    return of(stats);
  }

  private mapProductReview(dto: ProductReviewResponseDto): Review {
    return {
      id: dto.id,
      userName: dto.userName,
      userAvatar: dto.userAvatar ?? undefined,
      rating: dto.rating,
      comment: dto.comment,
      date: dto.date,
      verified: dto.verified ?? undefined,
    };
  }

  private mapProductReviewStats(dto: ProductReviewStatsDto | null | undefined): ReviewStats {
    if (!dto) {
      return this.emptyProductReviewStats();
    }
    return {
      averageRating: dto.averageRating,
      totalReviews: dto.totalReviews,
      distribution: this.normalizeDistribution(dto.distribution),
    };
  }

  private emptyProductReviewStats(): ReviewStats {
    return {
      averageRating: 0,
      totalReviews: 0,
      distribution: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 },
    };
  }

  private normalizeDistribution(raw: Record<string, number> | null | undefined): RatingDistribution {
    const d: RatingDistribution = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
    if (!raw) {
      return d;
    }
    for (let i = 1; i <= 5; i++) {
      const v = raw[i] ?? raw[String(i) as keyof typeof raw];
      d[i as keyof RatingDistribution] = typeof v === 'number' && !Number.isNaN(v) ? v : Number(v ?? 0);
    }
    return d;
  }
}
