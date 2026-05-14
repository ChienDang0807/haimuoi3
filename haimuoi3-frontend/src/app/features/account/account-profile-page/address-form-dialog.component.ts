import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { ErrorStateMatcher, ShowOnDirtyErrorStateMatcher } from '@angular/material/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Address, CreateAddressRequest } from '../../../shared/interfaces';

const PHONE_PATTERN = /^[0-9]{10,11}$/;
const ADDRESS_NAME_MAX = 50;

export interface AddressFormDialogData {
  address?: Address;
}

@Component({
  selector: 'app-address-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ isEditMode() ? 'Edit address' : 'Add new address' }}</h2>
    <mat-dialog-content class="dialog-body">
      <form [formGroup]="form" id="address-form" (ngSubmit)="submit()" autocomplete="off">
        <mat-form-field appearance="outline" class="w-full" [hideRequiredMarker]="true">
          <mat-label>Address label (optional)</mat-label>
          <input matInput formControlName="addressName" maxlength="50" />
          <mat-hint>e.g. Home, Office</mat-hint>
        </mat-form-field>

        <div class="grid grid-cols-2 gap-3">
          <mat-form-field appearance="outline" class="w-full" [hideRequiredMarker]="true">
            <mat-label>Recipient name</mat-label>
            <input matInput formControlName="recipientName" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full" [hideRequiredMarker]="true">
            <mat-label>Phone</mat-label>
            <input matInput formControlName="phone" inputmode="numeric" maxlength="11" />
            @if (form.controls.phone.dirty && form.controls.phone.errors?.['pattern']) {
              <mat-error>Phone must be 10-11 digits.</mat-error>
            }
          </mat-form-field>
        </div>

        <div class="grid grid-cols-3 gap-3">
          <mat-form-field appearance="outline" class="w-full" [hideRequiredMarker]="true">
            <mat-label>Province</mat-label>
            <input matInput formControlName="province" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="w-full" [hideRequiredMarker]="true">
            <mat-label>District</mat-label>
            <input matInput formControlName="district" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="w-full" [hideRequiredMarker]="true">
            <mat-label>Ward</mat-label>
            <input matInput formControlName="ward" />
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="w-full" [hideRequiredMarker]="true">
          <mat-label>Street address</mat-label>
          <textarea matInput formControlName="streetAddress" rows="2"></textarea>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="dialogRef.close()">Cancel</button>
      <button
        mat-flat-button
        color="primary"
        type="submit"
        form="address-form"
        [disabled]="form.invalid || submitting()">
        @if (submitting()) {
          <mat-progress-spinner mode="indeterminate" diameter="18" />
        } @else {
          <span>{{ isEditMode() ? 'Save changes' : 'Add address' }}</span>
        }
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .dialog-body {
        min-width: 480px;
        padding-top: 0.5rem;
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
      }
      .w-full {
        width: 100%;
      }
      ::ng-deep .mdc-notched-outline__notch {
        border-right: none !important;
      }
    `,
  ],
  providers: [{ provide: ErrorStateMatcher, useClass: ShowOnDirtyErrorStateMatcher }],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddressFormDialogComponent {
  readonly dialogRef = inject(MatDialogRef<AddressFormDialogComponent, CreateAddressRequest | null>);
  private readonly data = inject<AddressFormDialogData>(MAT_DIALOG_DATA, { optional: true }) ?? {};
  private readonly fb = inject(FormBuilder);

  readonly submitting = signal(false);
  readonly isEditMode = computed(() => !!this.data.address);

  readonly form = this.fb.nonNullable.group({
    addressName: ['', [Validators.maxLength(ADDRESS_NAME_MAX)]],
    recipientName: ['', [Validators.required, Validators.maxLength(100)]],
    phone: ['', [Validators.required, Validators.pattern(PHONE_PATTERN)]],
    province: ['', [Validators.required, Validators.maxLength(100)]],
    district: ['', [Validators.required, Validators.maxLength(100)]],
    ward: ['', [Validators.required, Validators.maxLength(100)]],
    streetAddress: ['', [Validators.required, Validators.maxLength(255)]],
  });

  constructor() {
    if (this.data.address) {
      const a = this.data.address;
      this.form.reset({
        addressName: a.addressName ?? '',
        recipientName: a.recipientName ?? '',
        phone: a.phone ?? '',
        province: a.province ?? '',
        district: a.district ?? '',
        ward: a.ward ?? '',
        streetAddress: a.streetAddress ?? '',
      });
    }
  }

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      return;
    }
    const v = this.form.getRawValue();
    const payload: CreateAddressRequest = {
      recipientName: v.recipientName.trim(),
      phone: v.phone.trim(),
      province: v.province.trim(),
      district: v.district.trim(),
      ward: v.ward.trim(),
      streetAddress: v.streetAddress.trim(),
    };
    const name = v.addressName.trim();
    if (name) {
      payload.addressName = name;
    }
    this.dialogRef.close(payload);
  }
}
