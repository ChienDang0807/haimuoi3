import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ShopCategoryDto, CreateShopProductPayload } from '../../../shared/interfaces';

export interface ShopAddProductDialogData {
  shopCategories: ShopCategoryDto[];
}

@Component({
  selector: 'app-shop-add-product-dialog',
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
  ],
  template: `
    <h2 mat-dialog-title>New product</h2>
    <form [formGroup]="form" (ngSubmit)="submit()">
      <mat-dialog-content class="dialog-body">
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Name</mat-label>
          <input matInput formControlName="name" />
        </mat-form-field>
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Price</mat-label>
          <input matInput type="number" step="0.01" formControlName="price" />
        </mat-form-field>
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Description</mat-label>
          <textarea matInput rows="3" formControlName="description"></textarea>
        </mat-form-field>
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Shop category</mat-label>
          <mat-select formControlName="shopCategoryId">
            @for (c of data.shopCategories; track c.shopCategoryId) {
              <mat-option [value]="c.shopCategoryId">{{ c.name }} ({{ c.slug }})</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Brand</mat-label>
          <input matInput formControlName="brand" />
        </mat-form-field>
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" (click)="dialogRef.close()">Cancel</button>
        <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid">Create</button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [
    `
      .dialog-body {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        min-width: 320px;
        padding-top: 0.5rem;
      }
      .w-full {
        width: 100%;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShopAddProductDialogComponent {
  readonly dialogRef = inject(MatDialogRef<ShopAddProductDialogComponent, CreateShopProductPayload | null>);
  readonly data = inject<ShopAddProductDialogData>(MAT_DIALOG_DATA);
  private fb = inject(FormBuilder);

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(500)]],
    price: [0, [Validators.required, Validators.min(0.01)]],
    description: [''],
    shopCategoryId: ['', Validators.required],
    brand: [''],
  });

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    const v = this.form.getRawValue();
    const payload: CreateShopProductPayload = {
      name: v.name.trim(),
      price: Number(v.price),
      productKind: 'LEGACY',
      shopCategoryId: v.shopCategoryId.trim(),
    };
    const desc = v.description.trim();
    if (desc) {
      payload.description = desc;
    }
    const brand = v.brand.trim();
    if (brand) {
      payload.brand = brand;
    }
    this.dialogRef.close(payload);
  }
}
