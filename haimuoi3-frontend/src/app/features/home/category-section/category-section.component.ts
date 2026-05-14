import { Component, Input, ChangeDetectionStrategy, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Category } from '../../../shared/interfaces';
import { CategoryCardComponent } from './category-card/category-card.component';

@Component({
  selector: 'app-category-section',
  standalone: true,
  imports: [CommonModule, CategoryCardComponent],
  templateUrl: './category-section.component.html',
  styleUrl: './category-section.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CategorySectionComponent {
  private router = inject(Router);

  @Input({ required: true }) categories: Category[] = [];
  
  // Tham chiếu đến container chứa danh sách category
  @ViewChild('scrollContainer') scrollContainer!: ElementRef<HTMLDivElement>;

  onCategoryClick(category: Category): void {
    this.router.navigate([category.route], {
      queryParams: {
        globalCategoryId: category.globalCategoryId,
        categoryName: category.name
      }
    });
  }

  // Trong file category-section.component.ts
onNavigatePrevious(): void {
  this.scrollContainer.nativeElement.scrollBy({
    left: -(this.scrollContainer.nativeElement.offsetWidth / 3),
    behavior: 'smooth'
  });
}

onNavigateNext(): void {
  this.scrollContainer.nativeElement.scrollBy({
    left: (this.scrollContainer.nativeElement.offsetWidth / 3),
    behavior: 'smooth'
  });
}
}