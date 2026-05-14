import { Component, inject, signal, ChangeDetectionStrategy, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';
import { CartSessionKeys } from '../../../core/constants';
import type { AddCartItemRequest } from '../../../shared/interfaces';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPageComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private cartService = inject(CartService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private platformId = inject(PLATFORM_ID);

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    emailOrPhone: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  onSubmit(): void {
    if (this.form.invalid || this.isLoading()) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();
    this.authService
      .login({
        emailOrPhone: raw.emailOrPhone.trim(),
        password: raw.password.trimEnd(),
      })
      .subscribe({
        next: () => {
          this.isLoading.set(false);
          const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/';
          const pending = this.consumePendingBuyNow();
          if (pending) {
            this.cartService.addItem(pending).subscribe({
              next: () => {
                this.router.navigateByUrl(returnUrl);
              },
              error: () => {
                this.router.navigateByUrl(returnUrl);
              },
            });
            return;
          }
          this.router.navigateByUrl(returnUrl);
        },
        error: (err) => {
          this.errorMessage.set(err.error?.message ?? 'Login failed. Please check your credentials.');
          this.isLoading.set(false);
        },
      });
  }

  private consumePendingBuyNow(): AddCartItemRequest | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }
    const raw = sessionStorage.getItem(CartSessionKeys.PENDING_BUY_NOW);
    if (!raw) {
      return null;
    }
    sessionStorage.removeItem(CartSessionKeys.PENDING_BUY_NOW);
    try {
      const parsed = JSON.parse(raw) as Partial<AddCartItemRequest>;
      if (
        parsed?.productId &&
        typeof parsed.productId === 'string' &&
        typeof parsed.unitPriceSnapshot === 'number' &&
        !Number.isNaN(parsed.unitPriceSnapshot)
      ) {
        const quantity = Math.max(1, Math.floor(Number(parsed.quantity) || 1));
        return {
          productId: parsed.productId,
          quantity,
          unitPriceSnapshot: parsed.unitPriceSnapshot,
        };
      }
    } catch {
      /* ignore malformed */
    }
    return null;
  }
}
