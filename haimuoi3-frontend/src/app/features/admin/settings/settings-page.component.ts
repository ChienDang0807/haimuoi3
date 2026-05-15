import { Component, ChangeDetectionStrategy, inject, signal, DestroyRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { AdminSidebarComponent } from '../../../shared/layout/admin-sidebar/admin-sidebar.component';
import { AdminHeaderComponent } from '../../../shared/layout/admin-header/admin-header.component';
import { NotificationSettingsComponent } from '../../../shared/components/notification-settings/notification-settings.component';
import { ShopOwnerApiService } from '../../../core/services/shop-owner-api.service';
import { ToastService } from '../../../core/services/toast.service';
import { ShopResponseDto, UpdateShopPayload } from '../../../shared/interfaces';

@Component({
  selector: 'app-settings-page',
  standalone: true,
  imports: [CommonModule, FormsModule, AdminSidebarComponent, AdminHeaderComponent, NotificationSettingsComponent],
  templateUrl: './settings-page.component.html',
  styleUrl: './settings-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SettingsPageComponent implements OnInit {
  private readonly shopOwnerApi = inject(ShopOwnerApiService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  readonly isLoading = signal(true);
  readonly isSaving = signal(false);
  readonly noShopLinked = signal(false);
  readonly isUploadingLogo = signal(false);
  readonly isUploadingBanner = signal(false);

  // Form model
  readonly form = signal<UpdateShopPayload>({});
  // Last loaded shop (for discard)
  private lastLoaded: ShopResponseDto | null = null;

  // Preview URLs
  readonly logoPreview = signal<string | null>(null);
  readonly bannerPreview = signal<string | null>(null);

  ngOnInit(): void {
    this.loadShop();
  }

  loadShop(): void {
    this.isLoading.set(true);
    this.noShopLinked.set(false);

    this.shopOwnerApi
      .getMyShop()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const shop = res.result;
          if (shop) {
            this.lastLoaded = shop;
            this.bindForm(shop);
            this.logoPreview.set(shop.logoUrl ?? null);
            this.bannerPreview.set(shop.bannerUrl ?? null);
          }
          this.isLoading.set(false);
        },
        error: (err) => {
          if (err?.status === 404) {
            this.noShopLinked.set(true);
          }
          this.isLoading.set(false);
        },
      });
  }

  saveSettings(): void {
    if (this.isSaving()) return;
    this.isSaving.set(true);

    this.shopOwnerApi
      .updateMyShop(this.form())
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isSaving.set(false)),
      )
      .subscribe({
        next: (res) => {
          if (res.result) {
            this.lastLoaded = res.result;
            this.bindForm(res.result);
            this.logoPreview.set(res.result.logoUrl ?? null);
            this.bannerPreview.set(res.result.bannerUrl ?? null);
          }
          this.toastService.success('Lưu cài đặt thành công');
        },
        error: (err) => {
          this.toastService.error(err?.error?.message ?? 'Lưu cài đặt thất bại. Vui lòng thử lại.');
        },
      });
  }

  discardChanges(): void {
    if (this.lastLoaded) {
      this.bindForm(this.lastLoaded);
      this.logoPreview.set(this.lastLoaded.logoUrl ?? null);
      this.bannerPreview.set(this.lastLoaded.bannerUrl ?? null);
    }
  }

  onBannerFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.uploadMedia(file, 'banner');
    input.value = '';
  }

  onLogoFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.uploadMedia(file, 'logo');
    input.value = '';
  }

  get isBusy(): boolean {
    return this.isSaving() || this.isUploadingLogo() || this.isUploadingBanner();
  }

  updateField(field: keyof UpdateShopPayload, value: string): void {
    this.form.update(f => ({ ...f, [field]: value }));
  }

  private uploadMedia(file: File, type: 'logo' | 'banner'): void {
    if (type === 'logo') {
      this.isUploadingLogo.set(true);
    } else {
      this.isUploadingBanner.set(true);
    }

    this.shopOwnerApi
      .uploadShopMedia(file)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          if (type === 'logo') this.isUploadingLogo.set(false);
          else this.isUploadingBanner.set(false);
        }),
      )
      .subscribe({
        next: (res) => {
          const url = res.result?.url;
          if (url) {
            if (type === 'logo') {
              this.form.update(f => ({ ...f, logoUrl: url }));
              this.logoPreview.set(url);
            } else {
              this.form.update(f => ({ ...f, bannerUrl: url }));
              this.bannerPreview.set(url);
            }
            this.toastService.success(type === 'logo' ? 'Tải logo thành công' : 'Tải banner thành công');
          }
        },
        error: () => {
          this.toastService.error('Tải ảnh thất bại. Vui lòng thử lại.');
        },
      });
  }

  private bindForm(shop: ShopResponseDto): void {
    this.form.set({
      shopName: shop.shopName ?? '',
      description: shop.description ?? '',
      email: shop.email ?? '',
      phone: shop.phone ?? '',
      province: shop.province ?? '',
      district: shop.district ?? '',
      addressDetail: shop.addressDetail ?? '',
      logoUrl: shop.logoUrl ?? '',
      bannerUrl: shop.bannerUrl ?? '',
    });
  }
}
