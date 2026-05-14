import { Component, input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Review, ReviewStats } from '../../../shared/interfaces';
import { RatingDisplayComponent } from '../../../shared/components/rating-display/rating-display.component';
import { ReviewCardComponent } from '../../../shared/components/review-card/review-card.component';

@Component({
  selector: 'app-product-reviews-section',
  standalone: true,
  imports: [CommonModule, RatingDisplayComponent, ReviewCardComponent],
  templateUrl: './product-reviews-section.component.html',
  styleUrl: './product-reviews-section.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductReviewsSectionComponent {
  reviews = input.required<Review[]>();
  stats = input.required<ReviewStats>();

  get ratingPercentages(): { rating: number; percentage: number }[] {
    const distribution = this.stats().distribution;
    const total = this.stats().totalReviews;
    
    return [5, 4, 3, 2, 1].map(rating => ({
      rating,
      percentage: total > 0 ? Math.round((distribution[rating] || 0) / total * 100) : 0
    }));
  }
}
