import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Testimonial } from '../../../shared/interfaces';
import { TestimonialCardComponent } from './testimonial-card/testimonial-card.component';

@Component({
  selector: 'app-testimonials',
  standalone: true,
  imports: [CommonModule, TestimonialCardComponent],
  templateUrl: './testimonials.component.html',
  styleUrl: './testimonials.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TestimonialsComponent {
  @Input({ required: true }) testimonials: Testimonial[] = [];
}
