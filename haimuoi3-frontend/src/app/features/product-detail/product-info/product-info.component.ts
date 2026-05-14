import { Component, input, output, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Product, ConfigOption } from '../../../shared/interfaces';
import { RatingDisplayComponent } from '../../../shared/components/rating-display/rating-display.component';
import { ProductConfigurationComponent } from '../product-configuration/product-configuration.component';

@Component({
  selector: 'app-product-info',
  standalone: true,
  imports: [CommonModule, RatingDisplayComponent, ProductConfigurationComponent],
  templateUrl: './product-info.component.html',
  styleUrl: './product-info.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductInfoComponent {
  product = input.required<Product>();
  
  buyNow = output<void>();
  addToCart = output<void>();
  configChange = output<ConfigOption>();

  onBuyNowClick(): void {
    this.buyNow.emit();
  }

  onAddToCartClick(): void {
    this.addToCart.emit();
  }

  onConfigurationChange(option: ConfigOption): void {
    this.configChange.emit(option);
  }
}
