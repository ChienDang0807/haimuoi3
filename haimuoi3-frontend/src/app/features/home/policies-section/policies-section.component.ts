import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Policy } from '../../../shared/interfaces';
import { PolicyCardComponent } from './policy-card/policy-card.component';

@Component({
  selector: 'app-policies-section',
  standalone: true,
  imports: [CommonModule, PolicyCardComponent],
  templateUrl: './policies-section.component.html',
  styleUrl: './policies-section.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PoliciesSectionComponent {
  @Input({ required: true }) mainPolicies: Policy[] = [];
  @Input({ required: true }) benefitPolicies: Policy[] = [];
}
