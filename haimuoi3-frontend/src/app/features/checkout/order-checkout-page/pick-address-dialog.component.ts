import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatRadioModule } from '@angular/material/radio';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FormsModule } from '@angular/forms';
import { AddressService } from '../../../core/services/address.service';
import { ToastService } from '../../../core/services/toast.service';
import { Address, CreateAddressRequest } from '../../../shared/interfaces';
import {
  AddressFormDialogComponent,
  AddressFormDialogData,
} from '../../account/account-profile-page/address-form-dialog.component';

export interface PickAddressDialogData {
  addresses: Address[];
  selectedId?: number;
}

@Component({
  selector: 'app-pick-address-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButtonModule,
    MatRadioModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>Choose shipping address</h2>
    <mat-dialog-content class="dialog-body">
      @if (busy()) {
        <div class="busy">
          <mat-progress-spinner mode="indeterminate" diameter="36" />
        </div>
      }
      @if (sortedAddresses().length === 0) {
        <div class="empty">
          <p>No saved addresses yet.</p>
        </div>
      } @else {
        <mat-radio-group [ngModel]="selectedId()" (ngModelChange)="selectedId.set($event)">
          @for (a of sortedAddresses(); track a.id) {
            <label class="address-row" [class.selected]="selectedId() === a.id">
              <mat-radio-button [value]="a.id" />
              <div class="address-body">
                <div class="row-head">
                  <span class="label">{{ a.addressName || 'Address' }}</span>
                  @if (a.isDefault) {
                    <span class="badge">Default</span>
                  }
                </div>
                <p class="recipient">{{ a.recipientName }} &middot; {{ a.phone }}</p>
                <p class="address-line">{{ formatAddress(a) }}</p>
              </div>
            </label>
          }
        </mat-radio-group>
      }

      <button
        type="button"
        class="add-new"
        [disabled]="busy()"
        (click)="openAddDialog()">
        + Add new address
      </button>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" [disabled]="busy()" (click)="dialogRef.close()">Cancel</button>
      <button
        mat-flat-button
        color="primary"
        type="button"
        [disabled]="busy() || selectedId() == null"
        (click)="confirmSelection()">
        Use this address
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
        gap: 0.5rem;
        max-height: 60vh;
      }
      .busy {
        display: flex;
        justify-content: center;
        padding: 1rem;
      }
      .empty {
        padding: 1.5rem;
        text-align: center;
        color: var(--mat-sys-on-surface-variant);
      }
      mat-radio-group {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
      }
      .address-row {
        display: flex;
        align-items: flex-start;
        gap: 0.5rem;
        padding: 0.75rem 1rem;
        border: 1px solid var(--mat-sys-outline-variant);
        border-radius: 0.5rem;
        cursor: pointer;
        transition: background-color 0.15s ease;
      }
      .address-row:hover {
        background: color-mix(in srgb, var(--mat-sys-primary) 4%, transparent);
      }
      .address-row.selected {
        border-color: var(--mat-sys-primary);
        background: color-mix(in srgb, var(--mat-sys-primary) 6%, transparent);
      }
      .address-body {
        flex: 1;
      }
      .row-head {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        margin-bottom: 0.25rem;
      }
      .label {
        font-size: 10px;
        font-weight: 900;
        text-transform: uppercase;
        letter-spacing: 0.1em;
      }
      .badge {
        font-size: 10px;
        font-weight: 900;
        text-transform: uppercase;
        letter-spacing: 0.1em;
        color: var(--mat-sys-primary);
        background: color-mix(in srgb, var(--mat-sys-primary) 10%, transparent);
        border-radius: 4px;
        padding: 0.125rem 0.5rem;
      }
      .recipient {
        font-size: 14px;
        font-weight: 500;
        margin: 0;
      }
      .address-line {
        font-size: 13px;
        color: var(--mat-sys-on-surface-variant);
        margin: 0.125rem 0 0 0;
      }
      .add-new {
        align-self: flex-start;
        margin-top: 0.75rem;
        font-size: 11px;
        font-weight: 900;
        text-transform: uppercase;
        letter-spacing: 0.1em;
        color: var(--mat-sys-primary);
        background: transparent;
        border: none;
        cursor: pointer;
      }
      .add-new:hover:not(:disabled) {
        text-decoration: underline;
      }
      .add-new:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PickAddressDialogComponent {
  readonly dialogRef = inject(MatDialogRef<PickAddressDialogComponent, Address | null>);
  private readonly data = inject<PickAddressDialogData>(MAT_DIALOG_DATA);
  private readonly addressService = inject(AddressService);
  private readonly toast = inject(ToastService);
  private readonly dialog = inject(MatDialog);

  readonly addresses = signal<Address[]>(this.data.addresses ?? []);
  readonly selectedId = signal<number | null>(this.data.selectedId ?? this.pickInitialId());
  readonly busy = signal(false);

  readonly sortedAddresses = computed(() =>
    [...this.addresses()].sort((a, b) => {
      if (a.isDefault !== b.isDefault) return a.isDefault ? -1 : 1;
      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
    }),
  );

  formatAddress(a: Address): string {
    return [a.streetAddress, a.ward, a.district, a.province].filter(Boolean).join(', ');
  }

  openAddDialog(): void {
    const ref = this.dialog.open<AddressFormDialogComponent, AddressFormDialogData, CreateAddressRequest | null>(
      AddressFormDialogComponent,
      { autoFocus: false },
    );
    ref.afterClosed().subscribe(payload => {
      if (!payload) return;
      this.busy.set(true);
      const wasEmpty = this.addresses().length === 0;
      this.addressService.createAddress(payload).subscribe({
        next: createRes => {
          const created = createRes.result;
          const afterDefault = () => {
            this.addressService.listMyAddresses().subscribe({
              next: listRes => {
                const list = listRes.result ?? [];
                this.addresses.set(list);
                if (created?.id) this.selectedId.set(created.id);
                this.busy.set(false);
                this.toast.success('Address added');
              },
              error: () => {
                this.busy.set(false);
              },
            });
          };
          if (wasEmpty && created?.id) {
            this.addressService.setDefaultAddress(created.id).subscribe({
              next: afterDefault,
              error: afterDefault,
            });
          } else {
            afterDefault();
          }
        },
        error: err => {
          this.busy.set(false);
          this.toast.error(err?.error?.message ?? 'Failed to add address');
        },
      });
    });
  }

  confirmSelection(): void {
    const id = this.selectedId();
    if (id == null) return;
    const picked = this.addresses().find(a => a.id === id) ?? null;
    this.dialogRef.close(picked);
  }

  private pickInitialId(): number | null {
    const list = this.data.addresses ?? [];
    const def = list.find(a => a.isDefault);
    return def?.id ?? list[0]?.id ?? null;
  }
}
