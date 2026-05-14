import { Component, input, output, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Shop } from '../../../shared/interfaces';

@Component({
  selector: 'app-shop-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './shop-header.component.html',
  styleUrl: './shop-header.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ShopHeaderComponent {
  shop = input.required<Shop>();
  
  follow = output<string>();
  message = output<string>();

  onFollowClick(): void {
    this.follow.emit(this.shop().id);
  }

  onMessageClick(): void {
    this.message.emit(this.shop().id);
  }
}
