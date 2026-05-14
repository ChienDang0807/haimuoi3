import { Injectable, inject, signal, computed, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, map, catchError, of } from 'rxjs';
import { 
  Cart, 
  CartItem, 
  AddCartItemRequest, 
  MergeCartRequest, 
  MergeCartResponse, 
  ApiResponse 
} from '../../shared/interfaces';
import { environment } from '../../../environments/environment';
import { ApiEndpoints, CartRules, CartErrorCodes } from '../constants';
import { ToastService } from './toast.service';
import { AuthService } from './auth.service';

/**
 * Cart Service với state management bằng signals
 * Reference: haimuoi3/PLAN-Cart-FE.md section 3
 */
@Injectable({
  providedIn: 'root'
})
export class CartService {
  private http = inject(HttpClient);
  private platformId = inject(PLATFORM_ID);
  private toastService = inject(ToastService);
  private authService = inject(AuthService);
  private readonly baseUrl = environment.apiUrl;
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  private cartState = signal<Cart | null>(null);
  private isLoadingState = signal(false);
  private pendingUpdates = new Map<string, any>();

  readonly cart = this.cartState.asReadonly();
  readonly isLoading = this.isLoadingState.asReadonly();
  
  readonly totalItems = computed(() => this.cartState()?.totalItems ?? 0);
  readonly items = computed(() => this.cartState()?.items ?? []);

  private getCartToken(): string {
    if (!this.isBrowser) {
      return '';
    }
    const match = document.cookie.match(new RegExp(`(^| )${CartRules.CART_TOKEN_COOKIE_NAME}=([^;]+)`));
    return match ? match[2] : '';
  }

  /**
   * Tạo hoặc lấy guest cart
   */
  createOrGetGuestCart(): Observable<Cart> {
    return this.http.post<ApiResponse<Cart>>(`${this.baseUrl}${ApiEndpoints.CART_SESSION}`, {})
      .pipe(
        map(res => res.result),
        tap(cart => this.cartState.set(cart)),
        catchError(err => {
          console.error('Failed to create/get guest cart', err);
          return of(null as any);
        })
      );
  }

  /**
   * Load cart từ server theo token
   */
  loadCart(cartToken?: string): Observable<Cart> {
    if (!this.isBrowser) {
      return of(null as any);
    }
    if (this.authService.isLoggedIn()) {
      return this.loadUserCart();
    }

    const token = cartToken || this.getCartToken();
    if (!token) {
      return this.createOrGetGuestCart();
    }

    this.isLoadingState.set(true);
    return this.http.get<ApiResponse<Cart>>(`${this.baseUrl}${ApiEndpoints.CART_SESSION}/${token}`)
      .pipe(
        map(res => res.result),
        tap(cart => {
          this.cartState.set(cart);
          this.isLoadingState.set(false);
        }),
        catchError(err => {
          this.isLoadingState.set(false);
          if (err.error?.code === CartErrorCodes.CART_NOT_FOUND) {
            return this.createOrGetGuestCart();
          }
          console.error('Failed to load cart', err);
          return of(null as any);
        })
      );
  }

  private loadUserCart(): Observable<Cart> {
    this.isLoadingState.set(true);
    return this.http.get<ApiResponse<Cart>>(`${this.baseUrl}${ApiEndpoints.CART_ME}`)
      .pipe(
        map(res => res.result),
        tap(cart => {
          this.cartState.set(cart);
          this.isLoadingState.set(false);
        }),
        catchError(err => {
          this.isLoadingState.set(false);
          console.error('Failed to load user cart', err);
          return of(null as any);
        })
      );
  }

  /**
   * Thêm item vào cart
   */
  addItem(request: AddCartItemRequest): Observable<Cart> {
    if (this.authService.isLoggedIn()) {
      return this.addUserItem(request);
    }

    this.isLoadingState.set(true);
    return this.http.post<ApiResponse<Cart>>(`${this.baseUrl}${ApiEndpoints.CART_SESSION_ITEMS}`, request)
      .pipe(
        map(res => res.result),
        tap(cart => {
          this.cartState.set(cart);
          this.isLoadingState.set(false);
        }),
        catchError(err => {
          this.isLoadingState.set(false);
          console.error('Failed to add item to cart', err);
          throw err;
        })
      );
  }

  private addUserItem(request: AddCartItemRequest): Observable<Cart> {
    this.isLoadingState.set(true);
    return this.http.post<ApiResponse<Cart>>(`${this.baseUrl}${ApiEndpoints.CART_ME_ITEMS}`, request)
      .pipe(
        map(res => res.result),
        tap(cart => {
          this.cartState.set(cart);
          this.isLoadingState.set(false);
        }),
        catchError(err => {
          this.isLoadingState.set(false);
          console.error('Failed to add item to user cart', err);
          throw err;
        })
      );
  }

  /**
   * Cập nhật số lượng item
   */
  updateItemQuantity(productId: string, quantity: number): Observable<Cart> {
    if (this.authService.isLoggedIn()) {
      return this.updateUserItemQuantity(productId, quantity);
    }

    const cartToken = this.getCartToken();
    if (!cartToken) {
      throw new Error('Cart token not found');
    }

    this.isLoadingState.set(true);
    return this.http.patch<ApiResponse<Cart>>(
      `${this.baseUrl}${ApiEndpoints.CART_SESSION}/${cartToken}/items/${productId}`,
      { quantity }
    ).pipe(
      map(res => res.result),
      tap(cart => {
        this.cartState.set(cart);
        this.isLoadingState.set(false);
      }),
      catchError(err => {
        this.isLoadingState.set(false);
        console.error('Failed to update item quantity', err);
        throw err;
      })
    );
  }

  private updateUserItemQuantity(productId: string, quantity: number): Observable<Cart> {
    this.isLoadingState.set(true);
    return this.http.patch<ApiResponse<Cart>>(
      `${this.baseUrl}${ApiEndpoints.CART_ME_ITEMS}/${productId}`,
      { quantity }
    ).pipe(
      map(res => res.result),
      tap(cart => {
        this.cartState.set(cart);
        this.isLoadingState.set(false);
      }),
      catchError(err => {
        this.isLoadingState.set(false);
        console.error('Failed to update user item quantity', err);
        throw err;
      })
    );
  }

  /**
   * Xóa item khỏi cart
   */
  removeItem(productId: string): Observable<Cart> {
    if (this.authService.isLoggedIn()) {
      return this.removeUserItem(productId);
    }

    const cartToken = this.getCartToken();
    if (!cartToken) {
      throw new Error('Cart token not found');
    }

    this.isLoadingState.set(true);
    return this.http.delete<ApiResponse<Cart>>(
      `${this.baseUrl}${ApiEndpoints.CART_SESSION}/${cartToken}/items/${productId}`
    ).pipe(
      map(res => res.result),
      tap(cart => {
        this.cartState.set(cart);
        this.isLoadingState.set(false);
      }),
      catchError(err => {
        this.isLoadingState.set(false);
        console.error('Failed to remove item', err);
        throw err;
      })
    );
  }

  private removeUserItem(productId: string): Observable<Cart> {
    this.isLoadingState.set(true);
    return this.http.delete<ApiResponse<Cart>>(
      `${this.baseUrl}${ApiEndpoints.CART_ME_ITEMS}/${productId}`
    ).pipe(
      map(res => res.result),
      tap(cart => {
        this.cartState.set(cart);
        this.isLoadingState.set(false);
      }),
      catchError(err => {
        this.isLoadingState.set(false);
        console.error('Failed to remove user item', err);
        throw err;
      })
    );
  }

  /**
   * Clear toàn bộ cart
   */
  clearCart(): Observable<void> {
    if (this.authService.isLoggedIn()) {
      return this.clearUserCart();
    }

    const cartToken = this.getCartToken();
    if (!cartToken) {
      throw new Error('Cart token not found');
    }

    return this.http.delete<void>(`${this.baseUrl}${ApiEndpoints.CART_SESSION}/${cartToken}`)
      .pipe(
        tap(() => this.cartState.set(null)),
        catchError(err => {
          console.error('Failed to clear cart', err);
          throw err;
        })
      );
  }

  private clearUserCart(): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}${ApiEndpoints.CART_ME}`)
      .pipe(
        tap(() => this.cartState.set(null)),
        catchError(err => {
          console.error('Failed to clear user cart', err);
          throw err;
        })
      );
  }

  /**
   * Merge guest cart vào user cart khi login
   * (Sẽ implement phase sau khi có authentication)
   */
  mergeCart(request: MergeCartRequest): Observable<MergeCartResponse> {
    return this.http.post<ApiResponse<MergeCartResponse>>(
      `${this.baseUrl}${ApiEndpoints.CART_MERGE}`,
      request
    ).pipe(
      map(res => res.result)
    );
  }

  /**
   * Optimistic update với debouncing cho quantity changes
   * UI updates immediately, API call debounced 800ms
   */
  updateItemQuantityOptimistic(productId: string, quantity: number): void {
    // Validate quantity
    if (quantity < CartRules.MIN_QUANTITY || quantity > CartRules.MAX_QUANTITY_PER_ITEM) {
      this.toastService.error(`Quantity must be between ${CartRules.MIN_QUANTITY} and ${CartRules.MAX_QUANTITY_PER_ITEM}`);
      return;
    }

    // Clear existing timer for this product
    if (this.pendingUpdates.has(productId)) {
      clearTimeout(this.pendingUpdates.get(productId));
    }

    // Backup current state for potential rollback
    const previousCart = this.cartState();
    if (!previousCart) {
      return;
    }

    // Optimistic update - update UI immediately
    this.cartState.update(cart => {
      if (!cart) return cart;

      const updatedItems = cart.items.map(item =>
        item.productId === productId
          ? { ...item, quantity }
          : item
      );

      const newTotalItems = updatedItems.reduce((sum, item) => sum + item.quantity, 0);

      return {
        ...cart,
        items: updatedItems,
        totalItems: newTotalItems,
        updatedAt: new Date().toISOString()
      };
    });

    // Schedule debounced API call
    const timer = setTimeout(() => {
      this.syncWithBackend(productId, quantity, previousCart);
    }, CartRules.DEBOUNCE_UPDATE_MS);

    this.pendingUpdates.set(productId, timer);
  }

  /**
   * Sync optimistic update with backend
   * Rollback on error
   */
  private syncWithBackend(productId: string, quantity: number, previousCart: Cart): void {
    this.updateItemQuantity(productId, quantity).subscribe({
      next: () => {
        this.pendingUpdates.delete(productId);
      },
      error: (err) => {
        console.error('Failed to sync cart with backend', err);
        
        // Rollback to previous state
        this.cartState.set(previousCart);
        this.pendingUpdates.delete(productId);
        
        // Show error toast
        this.toastService.error('Failed to update cart. Please try again.');
      }
    });
  }
}
