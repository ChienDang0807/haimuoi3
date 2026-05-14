import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Testimonial } from '../../../../shared/interfaces';

@Component({
  selector: 'app-testimonial-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './testimonial-card.component.html',
  styleUrl: './testimonial-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TestimonialCardComponent {
  @Input({ required: true }) testimonial!: Testimonial;
}
