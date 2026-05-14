import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MyShopService } from '../../../core/services/my-shop.service';
import { CreateShopCategoryPayload, GlobalCategoryDto } from '../../../shared/interfaces';

@Component({
  selector: 'app-shop-create-category-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>Create shop category</h2>
    <mat-dialog-content class="dialog-body">
      @if (loading()) {
        <mat-progress-spinner mode="indeterminate" diameter="36" />
      } @else {
        <form [formGroup]="form" id="create-shop-cat-form" (ngSubmit)="submit()">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Name</mat-label>
            <input matInput formControlName="name" autocomplete="off" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Slug</mat-label>
            <input matInput formControlName="slug" autocomplete="off" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Link to global (optional)</mat-label>
            <mat-select formControlName="globalCategoryId">
              <mat-option value="">— None —</mat-option>
              @for (c of globalCategories(); track c.globalCategoryId) {
                <mat-option [value]="c.globalCategoryId">{{ c.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
        </form>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="dialogRef.close()">Cancel</button>
      <button
        mat-flat-button
        color="primary"
        type="submit"
        form="create-shop-cat-form"
        [disabled]="loading() || form.invalid">
        Create
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .dialog-body {
        min-width: 320px;
        padding-top: 0.5rem;
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
      }
      .w-full {
        width: 100%;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShopCreateCategoryDialogComponent {
  readonly dialogRef = inject(MatDialogRef<ShopCreateCategoryDialogComponent, CreateShopCategoryPayload | null>);
  private fb = inject(FormBuilder);
  private myShop = inject(MyShopService);

  loading = signal(true);
  globalCategories = signal<GlobalCategoryDto[]>([]);

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(500)]],
    slug: ['', [Validators.required, Validators.maxLength(200)]],
    globalCategoryId: [''],
  });

  constructor() {
    this.myShop.getGlobalCategories(0, 200).subscribe({
      next: res => {
        const rows = (res.result?.content ?? []).filter(c => c.active !== false && c.isActive !== false);
        this.globalCategories.set(rows);
        this.loading.set(false);
      },
      error: () => {
        this.globalCategories.set([]);
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    const v = this.form.getRawValue();
    const payload: CreateShopCategoryPayload = {
      name: v.name.trim(),
      slug: v.slug.trim(),
    };
    const gid = v.globalCategoryId.trim();
    if (gid) {
      payload.globalCategoryId = gid;
    }
    this.dialogRef.close(payload);
  }
}
