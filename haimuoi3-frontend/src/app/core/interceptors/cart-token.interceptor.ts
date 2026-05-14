import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CartRules } from '../constants';

/**
 * Interceptor tự động gắn X-Cart-Token header vào request guest cart session (/api/v1/carts/session)
 * Reference: haimuoi3/PLAN-Cart-FE.md section 2
 */
@Injectable()
export class CartTokenInterceptor implements HttpInterceptor {
  private platformId = inject(PLATFORM_ID);
  private cartToken: string;

  constructor() {
    this.cartToken = this.getOrCreateToken();
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!req.url.includes('/api/v1/carts/session')) {
      return next.handle(req);
    }

    const cloned = req.clone({
      setHeaders: {
        [CartRules.CART_TOKEN_HEADER_NAME]: this.cartToken
      }
    });

    return next.handle(cloned);
  }

  private getOrCreateToken(): string {
    if (!isPlatformBrowser(this.platformId)) {
      return '';
    }
    
    const existing = this.readCookie(CartRules.CART_TOKEN_COOKIE_NAME);
    if (existing) {
      return existing;
    }

    const token = crypto.randomUUID();
    const maxAge = CartRules.GUEST_CART_TTL_DAYS * 24 * 60 * 60;
    document.cookie = `${CartRules.CART_TOKEN_COOKIE_NAME}=${token}; max-age=${maxAge}; path=/; SameSite=Lax`;
    
    return token;
  }

  private readCookie(name: string): string | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }
    const match = document.cookie.match(new RegExp(`(^| )${name}=([^;]+)`));
    return match ? match[2] : null;
  }

  /**
   * Public method để component/service có thể lấy token
   */
  getCartToken(): string {
    return this.cartToken;
  }
}
