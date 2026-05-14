import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Category } from '../../../../shared/interfaces';
import { prepareCategoryForDisplay } from '../../../../shared/utils/category.utils';

@Component({
  selector: 'app-category-card',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './category-card.component.html',
  styleUrl: './category-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CategoryCardComponent {
  // Using Angular signals for better change detection
  categoryInput = input.required<Category>({ alias: 'category' });
  
  @Output() categoryClick = new EventEmitter<Category>();

  // Computed property với fallback logic
  protected displayCategory = computed(() => 
    prepareCategoryForDisplay(this.categoryInput())
  );

  onCardClick(): void {
    this.categoryClick.emit(this.categoryInput());
  }
}
