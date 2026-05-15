import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
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
import { ShopCategoryDto, CreateShopProductPayload, ShopProductResponse } from '../../../shared/interfaces';

export interface ShopAddProductDialogData {
  shopCategories: ShopCategoryDto[];
  initialData?: ShopProductResponse;
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
    <h2 mat-dialog-title class="!text-2xl !font-black !tracking-tighter uppercase">
      {{ data.initialData ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới' }}
    </h2>
    <form [formGroup]="form" (ngSubmit)="submit()">
      <mat-dialog-content class="dialog-body py-4">
        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Tên sản phẩm</mat-label>
          <input matInput formControlName="name" placeholder="Ví dụ: Áo thun Polo" />
          @if (form.get('name')?.errors?.['required']) {
            <mat-error>Tên sản phẩm là bắt buộc</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Giá bán (VND)</mat-label>
          <input matInput type="number" formControlName="price" placeholder="0" />
          @if (form.get('price')?.errors?.['required']) {
            <mat-error>Giá bán là bắt buộc</mat-error>
          }
          @if (form.get('price')?.errors?.['min']) {
            <mat-error>Giá bán phải lớn hơn 0</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Mô tả ngắn</mat-label>
          <textarea matInput rows="3" formControlName="description" placeholder="Mô tả về sản phẩm..."></textarea>
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Danh mục cửa hàng</mat-label>
          <mat-select formControlName="shopCategoryId">
            @for (c of data.shopCategories; track c.shopCategoryId) {
              <mat-option [value]="c.shopCategoryId">{{ c.name }}</mat-option>
            }
          </mat-select>
          @if (form.get('shopCategoryId')?.errors?.['required']) {
            <mat-error>Vui lòng chọn danh mục</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="w-full">
          <mat-label>Thương hiệu</mat-label>
          <input matInput formControlName="brand" placeholder="Ví dụ: Nike, Adidas" />
        </mat-form-field>
      </mat-dialog-content>

      <mat-dialog-actions align="end" class="!pb-6 !px-6">
        <button mat-button type="button" (click)="dialogRef.close()" class="uppercase tracking-widest font-bold text-[11px]">
          Hủy
        </button>
        <button
          mat-flat-button
          color="primary"
          type="submit"
          [disabled]="form.invalid"
          class="!rounded-lg uppercase tracking-widest font-black text-[11px] px-6 py-2">
          {{ data.initialData ? 'Cập nhật' : 'Tạo mới' }}
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [
    `
      .dialog-body {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
        min-width: 380px;
      }
      .w-full {
        width: 100%;
      }
      ::ng-deep .mat-mdc-dialog-container {
        border-radius: 20px !important;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShopAddProductDialogComponent implements OnInit {
  readonly dialogRef = inject(MatDialogRef<ShopAddProductDialogComponent, CreateShopProductPayload | null>);
  readonly data = inject<ShopAddProductDialogData>(MAT_DIALOG_DATA);
  private fb = inject(FormBuilder);

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(500)]],
    price: [0, [Validators.required, Validators.min(1000)]], // Min 1000đ
    description: [''],
    shopCategoryId: ['', Validators.required],
    brand: [''],
  });

  ngOnInit(): void {
    if (this.data.initialData) {
      const d = this.data.initialData;
      this.form.patchValue({
        name: d.name,
        price: Number(d.price),
        description: d.description || '',
        shopCategoryId: d.shopCategoryId || '',
        brand: d.brand || '',
      });
    }
  }

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
