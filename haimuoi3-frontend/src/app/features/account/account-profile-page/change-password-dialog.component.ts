import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { ErrorStateMatcher, ShowOnDirtyErrorStateMatcher } from '@angular/material/core';
import { MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ProfileService } from '../../../core/services/profile.service';
import { ToastService } from '../../../core/services/toast.service';

const MIN_PASSWORD_LENGTH = 6;

type FieldKey = 'oldPassword' | 'newPassword' | 'newPasswordConfirm';

@Component({
  selector: 'app-change-password-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>Change password</h2>
    <mat-dialog-content class="dialog-body">
      <form [formGroup]="form" id="change-password-form" (ngSubmit)="submit()" autocomplete="off">
        <mat-form-field appearance="outline" class="w-full" [hideRequiredMarker]="true">
          <mat-label>Current password</mat-label>
          <input
            matInput
            formControlName="oldPassword"
            [type]="visible().oldPassword ? 'text' : 'password'"
            autocomplete="current-password" />
          <button
            mat-icon-button
            matSuffix
            type="button"
            tabindex="-1"
            (click)="toggle('oldPassword')"
            [attr.aria-label]="visible().oldPassword ? 'Hide password' : 'Show password'">
            <mat-icon>{{ visible().oldPassword ? 'visibility_off' : 'visibility' }}</mat-icon>
          </button>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full" [hideRequiredMarker]="true">
          <mat-label>New password</mat-label>
          <input
            matInput
            formControlName="newPassword"
            [type]="visible().newPassword ? 'text' : 'password'"
            autocomplete="new-password" />
          <button
            mat-icon-button
            matSuffix
            type="button"
            tabindex="-1"
            (click)="toggle('newPassword')"
            [attr.aria-label]="visible().newPassword ? 'Hide password' : 'Show password'">
            <mat-icon>{{ visible().newPassword ? 'visibility_off' : 'visibility' }}</mat-icon>
          </button>
          <mat-hint>At least {{ minLength }} chars, contains a letter and a number.</mat-hint>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full" [hideRequiredMarker]="true">
          <mat-label>Confirm new password</mat-label>
          <input
            matInput
            formControlName="newPasswordConfirm"
            [type]="visible().newPasswordConfirm ? 'text' : 'password'"
            autocomplete="new-password" />
          <button
            mat-icon-button
            matSuffix
            type="button"
            tabindex="-1"
            (click)="toggle('newPasswordConfirm')"
            [attr.aria-label]="visible().newPasswordConfirm ? 'Hide password' : 'Show password'">
            <mat-icon>{{ visible().newPasswordConfirm ? 'visibility_off' : 'visibility' }}</mat-icon>
          </button>
          @if (mismatch()) {
            <mat-error>Passwords do not match.</mat-error>
          }
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" [disabled]="submitting()" (click)="dialogRef.close()">Cancel</button>
      <button
        mat-flat-button
        color="primary"
        type="submit"
        form="change-password-form"
        [disabled]="submitting() || form.invalid || mismatch()">
        @if (submitting()) {
          <mat-progress-spinner mode="indeterminate" diameter="18" />
        } @else {
          <span>Update password</span>
        }
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .dialog-body {
        min-width: 360px;
        padding-top: 0.5rem;
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
      }
      .w-full {
        width: 100%;
      }
      input::-ms-reveal,
      input::-ms-clear {
        display: none;
        width: 0;
        height: 0;
      }
      ::ng-deep .mdc-notched-outline__notch {
        border-right: none !important;
      }
    `,
  ],
  providers: [{ provide: ErrorStateMatcher, useClass: ShowOnDirtyErrorStateMatcher }],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChangePasswordDialogComponent {
  readonly dialogRef = inject(MatDialogRef<ChangePasswordDialogComponent, boolean>);
  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(ProfileService);
  private readonly toast = inject(ToastService);

  readonly minLength = MIN_PASSWORD_LENGTH;
  readonly submitting = signal(false);
  readonly visible = signal<Record<FieldKey, boolean>>({
    oldPassword: false,
    newPassword: false,
    newPasswordConfirm: false,
  });

  readonly form = this.fb.nonNullable.group({
    oldPassword: ['', [Validators.required]],
    newPassword: ['', [Validators.required, Validators.minLength(MIN_PASSWORD_LENGTH)]],
    newPasswordConfirm: ['', [Validators.required]],
  });

  readonly mismatch = computed(() => {
    const v = this.form.getRawValue();
    return v.newPasswordConfirm.length > 0 && v.newPassword !== v.newPasswordConfirm;
  });

  toggle(key: FieldKey): void {
    this.visible.update(state => ({ ...state, [key]: !state[key] }));
  }

  submit(): void {
    if (this.form.invalid || this.mismatch() || this.submitting()) {
      return;
    }
    const v = this.form.getRawValue();
    this.submitting.set(true);
    this.profileService
      .changePassword({
        oldPassword: v.oldPassword,
        newPassword: v.newPassword,
        newPasswordConfirm: v.newPasswordConfirm,
      })
      .subscribe({
        next: () => {
          this.toast.success('Password updated');
          this.submitting.set(false);
          this.dialogRef.close(true);
        },
        error: err => {
          const msg = err?.error?.message ?? 'Failed to update password';
          this.toast.error(msg);
          this.submitting.set(false);
        },
      });
  }
}
