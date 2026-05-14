import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Policy } from '../../../../shared/interfaces';

@Component({
  selector: 'app-policy-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './policy-card.component.html',
  styleUrl: './policy-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PolicyCardComponent {
  @Input({ required: true }) policy!: Policy;
  @Input() variant: 'large' | 'small' = 'large';
}
