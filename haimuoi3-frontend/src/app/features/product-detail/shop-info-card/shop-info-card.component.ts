import { Component, input, output, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Shop } from '../../../shared/interfaces';
import { RatingDisplayComponent } from '../../../shared/components/rating-display/rating-display.component';

@Component({
  selector: 'app-shop-info-card',
  standalone: true,
  imports: [CommonModule, RatingDisplayComponent],
  templateUrl: './shop-info-card.component.html',
  styleUrl: './shop-info-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ShopInfoCardComponent {
  shop = input.required<Shop>();
  visitStore = output<string>();

  onVisitStoreClick(): void {
    this.visitStore.emit(this.shop().slug ?? this.shop().id);
  }
}
