import {
  Component,
  Input,
  signal,
  OnInit,
  OnDestroy,
  ChangeDetectionStrategy,
  inject,
  PLATFORM_ID
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterModule } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import { HeroSlide } from '../../../shared/interfaces';

@Component({
  selector: 'app-hero-section',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './hero-section.component.html',
  styleUrl: './hero-section.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HeroSectionComponent implements OnInit, OnDestroy {
  @Input({ required: true }) slides: HeroSlide[] = [];
  
  currentSlideIndex = signal<number>(0);
  private autoPlaySubscription?: Subscription;
  private platformId = inject(PLATFORM_ID);
  private readonly INTERVAL_TIME = 5000;

  ngOnInit(): void {
    this.startAutoPlay();
  }

  ngOnDestroy(): void {
    this.stopAutoPlay();
  }

  getCurrentSlide(): HeroSlide | undefined {
    return this.slides[this.currentSlideIndex()];
  }

  // Tách riêng logic chuyển slide
  nextSlide(): void {
    if (this.slides.length <= 1) return;
    const nextIndex = (this.currentSlideIndex() + 1) % this.slides.length;
    this.currentSlideIndex.set(nextIndex);
  }

  // Dùng Public để gọi từ template
  startAutoPlay(): void {
    if (!isPlatformBrowser(this.platformId) || this.slides.length <= 1) return;
    
    // Đảm bảo không có subscription nào đang chạy trước khi tạo mới
    this.stopAutoPlay();
    
    this.autoPlaySubscription = interval(this.INTERVAL_TIME).subscribe(() => {
      this.nextSlide();
    });
  }

  stopAutoPlay(): void {
    if (this.autoPlaySubscription) {
      this.autoPlaySubscription.unsubscribe();
      this.autoPlaySubscription = undefined;
    }
  }

  onSlideClick(index: number): void {
    this.currentSlideIndex.set(index);
    // Khi người dùng click, ta reset lại timer để slide mới được hiển thị trọn vẹn 5s
    this.startAutoPlay();
  }
}