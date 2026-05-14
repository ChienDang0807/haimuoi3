import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, of, map, catchError } from 'rxjs';
import { Category, ApiResponse, PageResponse } from '../../shared/interfaces';
import { mapBackendCategory } from '../../shared/utils/category.utils';
import { environment } from '../../../environments/environment';
import { ApiEndpoints } from '../constants';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private http = inject(HttpClient);
  private platformId = inject(PLATFORM_ID);
  private readonly apiUrl = `${environment.apiUrl}${ApiEndpoints.GLOBAL_CATEGORIES}`;

  getCategories(): Observable<Category[]> {
    if (!isPlatformBrowser(this.platformId)) {
      return of(this.getFallbackCategories());
    }

    return this.http.get<ApiResponse<PageResponse<any>>>(`${this.apiUrl}?size=20`)
      .pipe(
        map(response => response.result.content.map(mapBackendCategory)),
        catchError(error => {
          console.error('Failed to load categories from API, using fallback', error);
          return of(this.getFallbackCategories());
        })
      );
  }

  private getFallbackCategories(): Category[] {
    return [
      {
        globalCategoryId: '1',
        name: 'Precision Tech',
        slug: 'tech',
        subtitle: 'Mind-Performance',
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBqLHBrHDf4AN5io9Xx4FbS8X4_ddpkViAxVEfhUmvi0SiRrpzH5WpruOLRJ0aO09_xsRvIj7SfCLQRcuDX41yJB6U_yc0phyelVfez6g7yRQqG0qUKlOr3yl-aYmu-xmpTGXu5QBKY3UvQP7FxOq2C7LVYCa97LRY15FL6kpqqPS0SxhB2jYIASoHpzKMlvbvF_YiDnmIBqRb-i-Z5DoguLc5HXSn-cc48ph4cmBTbrGvQ1MJyTCTnjMwDqlMIelKO8d5yDdJMWg8',
        ctaText: 'Explore Hardware',
        route: '/category/tech',
        isActive: true
      },
      {
        globalCategoryId: '2',
        name: 'Infinite Wisdom',
        slug: 'books',
        subtitle: 'Knowledge Base',
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCnDtNYDmmDJuWtc8ngjJEUXiyO4iA9nNujkoYZ98tqifsPb1_5pEc3h__hwis8_5x3BsGi6n0Q2KV23uJus3ZQ0w8RanCK5FSFQa0d0tnv6zrWxJ9ECHHDx8ajgpVxVrq2NgvruSH5dldgU1vnK-WZ3riFDQcpMv-BsZMkKyf_BZL3LGJrXjaHAKuAjmCU4Ez0Undgy0OC7XpUygkUrMnT_u_C-tytYVbEz2QL7Zh9eOkfwpgGUOoDw63i5qMNygsJONQfKxosIaI',
        ctaText: 'Browse Hardware',
        route: '/category/books',
        isActive: true
      },
      {
        globalCategoryId: '3',
        name: 'Curated Interiors',
        slug: 'interiors',
        subtitle: 'Living Interiors',
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuASgKC1jYYirwyPr_OGlhtNwyKORIsTWtkZ0oVETLhhpD_yUlvbPmNQnMVfY2H8SD2jUq3m_xEDfGu9UHxD2j9_iE8O9_XeEc0zkZLN92uCWZwyXQkNdoOzNW3iRFOnp63L3g5K7b4dl04vtxMBdP2yUlgKsnuSNOkdnLH0PiQxI7o1jskpCYT6x7v_9qN0qIogIkx1T-LyGfR38fiYAs7X7-SVYG09KgBwDgPF5aJ1JNl0_ATsfvZHrCY-Cm69Ek7PMzKqTfOLm84',
        ctaText: 'Shop Collection',
        route: '/category/interiors',
        isActive: true
      },
      {
        globalCategoryId: '4',
        name: 'Kitchen Mastery',
        slug: 'kitchen',
        subtitle: 'Bespoke Design',
        imageUrl: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCWUZ0nhm1NBebjNfDHyr0Ed_NXk0JaFrvOfRHzf3cdz1N3-NGlTSW4lBFpCvns-VmHkij3BmUxVzX-OOAAd18GrIWLH8XRPPTZP5BxU1P86UvOLddtdC3J6IQu6Q4Y-chPdtgF7UXZDsGUcGYCAhgRcynyAvjK7-pylCH5YUXsrZiVkVwfJSLMEQSlGEszmsmEsT80qoXvRwvYFQi3Sb95gC37OeJSf20owjcZsuTb5ZLE4Evn8izqfSxT-KAsLzx7j531bdv3PpM',
        ctaText: 'Browse Division',
        route: '/category/kitchen',
        isActive: true
      }
    ];
  }
}
