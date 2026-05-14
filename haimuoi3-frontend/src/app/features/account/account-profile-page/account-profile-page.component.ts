import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { HeaderComponent } from '../../../shared/layout/header/header.component';
import { AccountSidebarComponent } from '../account-sidebar/account-sidebar.component';
import { ProfileService } from '../../../core/services/profile.service';
import { AddressService } from '../../../core/services/address.service';
import { CustomerOrderService } from '../../../core/services/customer-order.service';
import { ToastService } from '../../../core/services/toast.service';
import { Address, CreateAddressRequest, Gender, OrderDetail } from '../../../shared/interfaces';
import { ChangePasswordDialogComponent } from './change-password-dialog.component';
import { AddressFormDialogComponent, AddressFormDialogData } from './address-form-dialog.component';
import { orderStatusBadgeClass, orderStatusLabel } from '../account-order-status.util';

const RECENT_ORDER_COUNT = 3;
const GENDER_OPTIONS: readonly Gender[] = ['MALE', 'FEMALE', 'OTHER'];

@Component({
  selector: 'app-account-profile-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, HeaderComponent, AccountSidebarComponent],
  templateUrl: './account-profile-page.component.html',
  styleUrl: './account-profile-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountProfilePageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(ProfileService);
  private readonly addressService = inject(AddressService);
  private readonly customerOrderService = inject(CustomerOrderService);
  private readonly toast = inject(ToastService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);

  readonly genderOptions = GENDER_OPTIONS;

  readonly profile = this.profileService.currentProfile;
  readonly isLoadingProfile = signal(true);
  readonly isEditing = signal(false);
  readonly isSaving = signal(false);

  readonly addresses = signal<Address[]>([]);
  readonly isLoadingAddresses = signal(true);
  readonly sortedAddresses = computed(() =>
    [...this.addresses()].sort((a, b) => {
      if (a.isDefault !== b.isDefault) return a.isDefault ? -1 : 1;
      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
    }),
  );

  readonly recentOrders = signal<OrderDetail[]>([]);
  readonly isLoadingOrders = signal(true);

  readonly todayIso = new Date().toISOString().slice(0, 10);

  readonly form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(100)]],
    dateOfBirth: [''],
    gender: [''],
  });

  readonly genderLabel = computed(() => this.formatGender(this.profile()?.gender ?? null));

  ngOnInit(): void {
    this.loadProfile();
    this.loadAddresses();
    this.loadRecentOrders();
  }

  enterEdit(): void {
    const p = this.profile();
    if (!p) return;
    this.form.reset({
      fullName: p.fullName ?? '',
      dateOfBirth: p.dateOfBirth ?? '',
      gender: p.gender ?? '',
    });
    this.isEditing.set(true);
  }

  cancelEdit(): void {
    this.isEditing.set(false);
    this.form.reset();
  }

  saveProfile(): void {
    if (this.form.invalid || this.isSaving()) {
      return;
    }
    const v = this.form.getRawValue();
    const dob = v.dateOfBirth?.trim() || null;
    if (dob && dob > this.todayIso) {
      this.toast.error('Date of birth cannot be in the future');
      return;
    }
    const gender = v.gender ? (v.gender as Gender) : null;

    this.isSaving.set(true);
    this.profileService
      .updateProfile({
        fullName: v.fullName.trim(),
        dateOfBirth: dob,
        gender,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toast.success('Profile updated');
          this.isSaving.set(false);
          this.isEditing.set(false);
        },
        error: err => {
          const msg = err?.error?.message ?? 'Failed to update profile';
          this.toast.error(msg);
          this.isSaving.set(false);
        },
      });
  }

  openChangePasswordDialog(): void {
    this.dialog.open(ChangePasswordDialogComponent, {
      width: '420px',
      autoFocus: false,
    });
  }

  openAddAddressDialog(): void {
    const ref = this.dialog.open<AddressFormDialogComponent, AddressFormDialogData, CreateAddressRequest | null>(
      AddressFormDialogComponent,
      { autoFocus: false },
    );
    ref.afterClosed().subscribe(payload => {
      if (!payload) return;
      const wasEmpty = this.addresses().length === 0;
      this.addressService.createAddress(payload).subscribe({
        next: res => {
          const created = res.result;
          if (wasEmpty && created?.id) {
            this.addressService.setDefaultAddress(created.id).subscribe({
              next: () => {
                this.toast.success('Address added');
                this.loadAddresses();
              },
              error: () => {
                this.toast.success('Address added');
                this.loadAddresses();
              },
            });
          } else {
            this.toast.success('Address added');
            this.loadAddresses();
          }
        },
        error: err => {
          const msg = err?.error?.message ?? 'Failed to add address';
          this.toast.error(msg);
        },
      });
    });
  }

  openEditAddressDialog(addr: Address): void {
    const ref = this.dialog.open<AddressFormDialogComponent, AddressFormDialogData, CreateAddressRequest | null>(
      AddressFormDialogComponent,
      { autoFocus: false, data: { address: addr } },
    );
    ref.afterClosed().subscribe(payload => {
      if (!payload) return;
      this.addressService.updateAddress(addr.id, payload).subscribe({
        next: () => {
          this.toast.success('Address updated');
          this.loadAddresses();
        },
        error: err => {
          const msg = err?.error?.message ?? 'Failed to update address';
          this.toast.error(msg);
        },
      });
    });
  }

  setDefault(addr: Address): void {
    if (addr.isDefault) return;
    this.addressService.setDefaultAddress(addr.id).subscribe({
      next: () => {
        this.toast.success('Default address updated');
        this.loadAddresses();
      },
      error: err => {
        const msg = err?.error?.message ?? 'Failed to set default';
        this.toast.error(msg);
      },
    });
  }

  deleteAddress(addr: Address): void {
    if (addr.isDefault) {
      const ok = window.confirm('This is your default address. Delete it anyway?');
      if (!ok) return;
    }
    this.addressService.deleteAddress(addr.id).subscribe({
      next: () => {
        this.toast.success('Address deleted');
        this.addressService.listMyAddresses().subscribe({
          next: res => {
            const remaining = res.result ?? [];
            this.addresses.set(remaining);
            this.isLoadingAddresses.set(false);
            const hasDefault = remaining.some(a => a.isDefault);
            if (!hasDefault && remaining.length > 0) {
              const firstId = [...remaining].sort(
                (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
              )[0].id;
              this.addressService.setDefaultAddress(firstId).subscribe({
                next: () => this.loadAddresses(),
                error: () => this.loadAddresses(),
              });
            }
          },
          error: () => this.loadAddresses(),
        });
      },
      error: err => {
        const msg = err?.error?.message ?? 'Failed to delete address';
        this.toast.error(msg);
      },
    });
  }

  formatGender(gender: Gender | string | null | undefined): string {
    if (!gender) return '—';
    switch (gender) {
      case 'MALE':
        return 'Male';
      case 'FEMALE':
        return 'Female';
      case 'OTHER':
        return 'Other';
      default:
        return gender;
    }
  }

  formatDate(value: string | null | undefined): string {
    if (!value) return '—';
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return value;
    return d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: '2-digit' });
  }

  formatPrice(value: number | string | null | undefined): string {
    if (value === null || value === undefined) return '—';
    const n = typeof value === 'string' ? parseFloat(value) : value;
    if (Number.isNaN(n)) return '—';
    return new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' }).format(n);
  }

  formatAddress(a: Address): string {
    return [a.streetAddress, a.ward, a.district, a.province].filter(Boolean).join(', ');
  }

  orderStatusLabel(status: string): string {
    return orderStatusLabel(status);
  }

  orderStatusBadgeClass(status: string): string {
    return orderStatusBadgeClass(status);
  }

  trackOrderById = (_: number, item: OrderDetail) => item.id;
  trackAddressById = (_: number, item: Address) => item.id;

  private loadProfile(): void {
    this.isLoadingProfile.set(true);
    this.profileService
      .getProfile()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.isLoadingProfile.set(false),
        error: err => {
          this.isLoadingProfile.set(false);
          const msg = err?.error?.message ?? 'Could not load profile';
          this.toast.error(msg);
        },
      });
  }

  private loadAddresses(): void {
    this.isLoadingAddresses.set(true);
    this.addressService
      .listMyAddresses()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: res => {
          this.addresses.set(res.result ?? []);
          this.isLoadingAddresses.set(false);
        },
        error: () => {
          this.addresses.set([]);
          this.isLoadingAddresses.set(false);
        },
      });
  }

  private loadRecentOrders(): void {
    this.isLoadingOrders.set(true);
    this.customerOrderService
      .listMyOrders(0, RECENT_ORDER_COUNT)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: res => {
          this.recentOrders.set(res.result?.content ?? []);
          this.isLoadingOrders.set(false);
        },
        error: () => {
          this.recentOrders.set([]);
          this.isLoadingOrders.set(false);
        },
      });
  }
}
