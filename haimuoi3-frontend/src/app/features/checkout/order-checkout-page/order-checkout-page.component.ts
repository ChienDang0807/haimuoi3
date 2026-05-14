import { ChangeDetectionStrategy, Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { HeaderComponent } from '../../../shared/layout/header/header.component';
import { FooterComponent } from '../../../shared/layout/footer/footer.component';
import { CartService } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';
import { ProductService } from '../../../core/services/product.service';
import { AddressService } from '../../../core/services/address.service';
import { ToastService } from '../../../core/services/toast.service';
import { Product } from '../../../shared/interfaces/product.interface';
import {
  Address,
  ApiResponse,
  CheckoutBatchResponse,
  CheckoutOrderRequest,
  CreateAddressRequest,
  StripeCheckoutSessionResponse,
} from '../../../shared/interfaces';
import { environment } from '../../../../environments/environment';
import { ApiEndpoints } from '../../../core/constants/api-endpoints';
import { CartItem } from '../../../shared/interfaces/cart.interface';
import { PickAddressDialogComponent, PickAddressDialogData } from './pick-address-dialog.component';
import {
  AddressFormDialogComponent,
  AddressFormDialogData,
} from '../../account/account-profile-page/address-form-dialog.component';

interface DisplayItem {
  productId: string;
  name: string;
  imageUrl: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
  shopId: string;
}

@Component({
  selector: 'app-order-checkout-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, HeaderComponent, FooterComponent],
  templateUrl: './order-checkout-page.component.html',
  styleUrl: './order-checkout-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrderCheckoutPageComponent implements OnInit {
  private cartService = inject(CartService);
  private authService = inject(AuthService);
  private productService = inject(ProductService);
  private addressService = inject(AddressService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);
  private dialog = inject(MatDialog);

  displayItems = signal<DisplayItem[]>([]);
  isLoadingItems = signal(true);
  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);
  /** Đơn STRIPE còn cần thanh toán (khi checkout đa shop). */
  stripePaymentOrderIds = signal<number[]>([]);

  readonly addresses = signal<Address[]>([]);
  readonly selectedAddress = signal<Address | null>(null);
  readonly isLoadingAddresses = signal(true);

  readonly subtotal = computed(() =>
    this.displayItems().reduce((sum, item) => sum + item.subtotal, 0)
  );
  readonly shippingCost = signal(0);
  readonly total = computed(() => this.subtotal() + this.shippingCost());

  form = this.fb.nonNullable.group({
    paymentMethod: ['COD' as 'COD' | 'STRIPE', [Validators.required]],
  });

  ngOnInit(): void {
    const user = this.authService.currentUser();
    if (user) {
      this.loadAddresses();
      this.cartService.loadCart().subscribe({
        next: () => this.loadProductDetails(),
        error: () => this.loadProductDetails(),
      });
      return;
    }

    if (this.cartService.items().length === 0) {
      this.cartService.loadCart().subscribe(() => this.loadProductDetails());
    } else {
      this.loadProductDetails();
    }
  }

  private loadAddresses(): void {
    this.isLoadingAddresses.set(true);
    this.addressService.listMyAddresses().subscribe({
      next: res => {
        const list = res.result ?? [];
        this.addresses.set(list);
        this.selectedAddress.set(list.find(a => a.isDefault) ?? list[0] ?? null);
        this.isLoadingAddresses.set(false);
      },
      error: () => {
        this.addresses.set([]);
        this.selectedAddress.set(null);
        this.isLoadingAddresses.set(false);
      },
    });
  }

  openPickAddressDialog(): void {
    const ref = this.dialog.open<PickAddressDialogComponent, PickAddressDialogData, Address | null>(
      PickAddressDialogComponent,
      {
        autoFocus: false,
        data: { addresses: this.addresses(), selectedId: this.selectedAddress()?.id },
      },
    );
    ref.afterClosed().subscribe(picked => {
      // Refresh list trong mọi trường hợp (có thể user đã add mới trong dialog).
      this.addressService.listMyAddresses().subscribe({
        next: res => {
          const list = res.result ?? [];
          this.addresses.set(list);
          if (picked) {
            const fresh = list.find(a => a.id === picked.id) ?? picked;
            this.selectedAddress.set(fresh);
          } else if (!this.selectedAddress() || !list.find(a => a.id === this.selectedAddress()!.id)) {
            this.selectedAddress.set(list.find(a => a.isDefault) ?? list[0] ?? null);
          }
        },
        error: () => {
          if (picked) this.selectedAddress.set(picked);
        },
      });
    });
  }

  openAddAddressShortcut(): void {
    const ref = this.dialog.open<AddressFormDialogComponent, AddressFormDialogData, CreateAddressRequest | null>(
      AddressFormDialogComponent,
      { autoFocus: false },
    );
    ref.afterClosed().subscribe(payload => {
      if (!payload) return;
      const wasEmpty = this.addresses().length === 0;
      this.addressService.createAddress(payload).subscribe({
        next: createRes => {
          const created = createRes.result;
          const finishLoad = () => {
            this.addressService.listMyAddresses().subscribe({
              next: res => {
                const list = res.result ?? [];
                this.addresses.set(list);
                if (created?.id) {
                  this.selectedAddress.set(list.find(a => a.id === created.id) ?? created);
                } else {
                  this.selectedAddress.set(list.find(a => a.isDefault) ?? list[0] ?? null);
                }
                this.toast.success('Address added');
              },
            });
          };
          if (wasEmpty && created?.id) {
            this.addressService.setDefaultAddress(created.id).subscribe({
              next: finishLoad,
              error: finishLoad,
            });
          } else {
            finishLoad();
          }
        },
        error: err => {
          this.toast.error(err?.error?.message ?? 'Failed to add address');
        },
      });
    });
  }

  formatShippingAddress(a: Address): string {
    const line = [a.streetAddress, a.ward, a.district, a.province].filter(Boolean).join(', ');
    return `${a.recipientName} (${a.phone}) - ${line}`;
  }

  private loadProductDetails(): void {
    const cartItems = this.cartService.items();
    if (cartItems.length === 0) {
      this.isLoadingItems.set(false);
      return;
    }

    const ids = cartItems.map(i => i.productId);
    this.productService.getProductsByIds(ids).subscribe({
      next: (products) => {
        const productMap = new Map<string, Product>(products.map(p => [p.id, p]));
        const items: DisplayItem[] = cartItems.map(cartItem => {
          const ci = cartItem as CartItem;
          const product = productMap.get(cartItem.productId);
          const name = ci.productNameSnapshot ?? product?.name ?? cartItem.productId;
          const shopId = ci.shopId ?? product?.shopId ?? '1';
          return {
            productId: cartItem.productId,
            name,
            imageUrl: product?.imageUrl ?? '',
            quantity: cartItem.quantity,
            unitPrice: cartItem.unitPriceSnapshot,
            subtotal: cartItem.quantity * cartItem.unitPriceSnapshot,
            shopId,
          };
        });
        this.displayItems.set(items);
        this.isLoadingItems.set(false);
      },
      error: () => {
        const items: DisplayItem[] = cartItems.map(ci => {
          const c = ci as CartItem;
          return {
            productId: ci.productId,
            name: c.productNameSnapshot ?? ci.productId,
            imageUrl: '',
            quantity: ci.quantity,
            unitPrice: ci.unitPriceSnapshot,
            subtotal: ci.quantity * ci.unitPriceSnapshot,
            shopId: c.shopId ?? '1',
          };
        });
        this.displayItems.set(items);
        this.isLoadingItems.set(false);
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid || this.isSubmitting() || this.displayItems().length === 0) return;

    const user = this.authService.currentUser();
    if (!user) return;

    const address = this.selectedAddress();
    if (!address) {
      this.errorMessage.set('Please select a shipping address.');
      return;
    }

    const cart = this.cartService.cart();
    if (!cart?.cartId) {
      this.errorMessage.set('Cart not ready. Please refresh the page.');
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);
    this.stripePaymentOrderIds.set([]);

    const { paymentMethod } = this.form.getRawValue();

    const checkoutBody: CheckoutOrderRequest = {
      cartId: cart.cartId,
      customerName: address.recipientName,
      shippingAddress: this.formatShippingAddress(address),
      paymentMethod,
    };

    this.http
      .post<ApiResponse<CheckoutBatchResponse>>(
        `${environment.apiUrl}${ApiEndpoints.ORDERS_CHECKOUT}`,
        checkoutBody
      )
      .subscribe({
        next: res => {
          const batch = res.result;
          const orders = batch.orders ?? [];
          this.cartService.loadCart().subscribe(() => {});

          if (paymentMethod === 'STRIPE') {
            if (orders.length === 1) {
              this.createStripeSession(orders[0].id);
              return;
            }
            this.isSubmitting.set(false);
            this.stripePaymentOrderIds.set(orders.map(o => o.id));
          } else {
            this.isSubmitting.set(false);
            const firstId = orders[0]?.id;
            this.router.navigate(['/order-confirmation'], {
              queryParams: {
                orderId: firstId,
                checkoutBatchId: batch.checkoutBatchId,
              },
            });
          }
        },
        error: err => {
          this.errorMessage.set(err.error?.message ?? 'Failed to checkout. Please try again.');
          this.isSubmitting.set(false);
        },
      });
  }

  startStripeForOrder(orderId: number): void {
    this.createStripeSession(orderId);
  }

  private createStripeSession(orderId: number): void {
    this.isSubmitting.set(true);
    this.errorMessage.set(null);
    this.http
      .post<ApiResponse<StripeCheckoutSessionResponse>>(
        `${environment.apiUrl}${ApiEndpoints.PAYMENTS_STRIPE_SESSION}`,
        { orderId }
      )
      .subscribe({
        next: res => {
          window.location.href = res.result.checkoutUrl;
        },
        error: err => {
          this.errorMessage.set(err.error?.message ?? 'Failed to start payment. Please try again.');
          this.isSubmitting.set(false);
        },
      });
  }
}
