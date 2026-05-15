import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ShopOwnerApiService } from '../../../core/services/shop-owner-api.service';
import { CreateShopCategoryPayload, GlobalCategoryDto, ShopCategoryDto } from '../../../shared/interfaces';

export interface ShopCategoryDialogData {
  initialData?: ShopCategoryDto;
}

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
    <h2 mat-dialog-title class="!text-2xl !font-black !tracking-tighter uppercase">
      {{ data?.initialData ? 'Chỉnh sửa danh mục' : 'Thêm danh mục mới' }}
    </h2>
    <mat-dialog-content class="dialog-body py-4">
      @if (loading()) {
        <div class="flex justify-center py-10">
          <mat-progress-spinner mode="indeterminate" diameter="36" />
        </div>
      } @else {
        <form [formGroup]="form" id="create-shop-cat-form" (ngSubmit)="submit()">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Tên danh mục</mat-label>
            <input matInput formControlName="name" placeholder="Ví dụ: Đồ gia dụng" autocomplete="off" />
            @if (form.get('name')?.errors?.['required']) {
              <mat-error>Tên danh mục là bắt buộc</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Đường dẫn (Slug)</mat-label>
            <input matInput formControlName="slug" placeholder="Ví dụ: do-gia-dung" autocomplete="off" />
            @if (form.get('slug')?.errors?.['required']) {
              <mat-error>Slug là bắt buộc</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Thứ tự hiển thị</mat-label>
            <input matInput type="number" formControlName="displayOrder" placeholder="0" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Liên kết danh mục hệ thống (Tùy chọn)</mat-label>
            <mat-select formControlName="globalCategoryId">
              <mat-option value="">— Không liên kết —</mat-option>
              @for (c of globalCategories(); track c.globalCategoryId) {
                <mat-option [value]="c.globalCategoryId">{{ c.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>URL Hình ảnh (Tùy chọn)</mat-label>
            <input matInput formControlName="imageUrl" placeholder="https://..." autocomplete="off" />
          </mat-form-field>
        </form>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end" class="!pb-6 !px-6">
      <button mat-button type="button" (click)="dialogRef.close()" class="uppercase tracking-widest font-bold text-[11px]">
        Hủy
      </button>
      <button
        mat-flat-button
        color="primary"
        type="submit"
        form="create-shop-cat-form"
        [disabled]="loading() || form.invalid"
        class="!rounded-lg uppercase tracking-widest font-black text-[11px] px-6 py-2">
        {{ data?.initialData ? 'Cập nhật' : 'Tạo mới' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .dialog-body {
        min-width: 380px;
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
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
export class ShopCreateCategoryDialogComponent implements OnInit {
  readonly dialogRef = inject(MatDialogRef<ShopCreateCategoryDialogComponent, CreateShopCategoryPayload | null>);
  readonly data = inject<ShopCategoryDialogData>(MAT_DIALOG_DATA, { optional: true });
  private fb = inject(FormBuilder);
  private shopOwnerApi = inject(ShopOwnerApiService);

  loading = signal(true);
  globalCategories = signal<GlobalCategoryDto[]>([]);

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(500)]],
    slug: ['', [Validators.required, Validators.maxLength(200)]],
    displayOrder: [0],
    imageUrl: [''],
    globalCategoryId: [''],
  });

  ngOnInit(): void {
    if (this.data?.initialData) {
      const d = this.data.initialData;
      this.form.patchValue({
        name: d.name,
        slug: d.slug,
        displayOrder: d.displayOrder || 0,
        imageUrl: d.imageUrl || '',
        globalCategoryId: d.globalCategoryId || '',
      });
    }

    this.shopOwnerApi.getGlobalCategories(0, 200).subscribe({
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
      displayOrder: v.displayOrder,
      imageUrl: v.imageUrl?.trim() || undefined,
    };
    const gid = v.globalCategoryId.trim();
    if (gid) {
      payload.globalCategoryId = gid;
    }
    this.dialogRef.close(payload);
  }
}
