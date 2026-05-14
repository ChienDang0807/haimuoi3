import { Component, input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Review } from '../../interfaces';
import { RatingDisplayComponent } from '../rating-display/rating-display.component';

@Component({
  selector: 'app-review-card',
  standalone: true,
  imports: [CommonModule, RatingDisplayComponent],
  templateUrl: './review-card.component.html',
  styleUrl: './review-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReviewCardComponent {
  review = input.required<Review>();

  get userInitials(): string {
    const name = this.review().userName;
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return parts[0][0] + parts[parts.length - 1][0];
    }
    return name.substring(0, 2);
  }
}
