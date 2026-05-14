import { Component, input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-rating-display',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './rating-display.component.html',
  styleUrl: './rating-display.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RatingDisplayComponent {
  rating = input.required<number>();
  size = input<'sm' | 'md' | 'lg'>('md');
  showValue = input<boolean>(false);

  get stars(): number[] {
    return [1, 2, 3, 4, 5];
  }

  getStarType(index: number): 'filled' | 'half' | 'empty' {
    const rating = this.rating();
    if (index <= Math.floor(rating)) {
      return 'filled';
    } else if (index === Math.ceil(rating) && rating % 1 !== 0) {
      return 'half';
    }
    return 'empty';
  }

  get sizeClass(): string {
    const sizes = {
      sm: 'text-sm',
      md: 'text-xl',
      lg: 'text-3xl'
    };
    return sizes[this.size()];
  }
}
