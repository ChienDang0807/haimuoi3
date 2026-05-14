import { Component, input, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-product-gallery',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-gallery.component.html',
  styleUrl: './product-gallery.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductGalleryComponent {
  images = input.required<string[]>();
  badge = input<string>();
  
  selectedImageIndex = signal<number>(0);

  selectImage(index: number): void {
    this.selectedImageIndex.set(index);
  }

  get mainImage(): string {
    return this.images()[this.selectedImageIndex()];
  }
}
