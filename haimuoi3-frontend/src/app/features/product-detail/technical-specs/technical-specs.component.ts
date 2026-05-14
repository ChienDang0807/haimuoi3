import { Component, input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Specification } from '../../../shared/interfaces';

@Component({
  selector: 'app-technical-specs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './technical-specs.component.html',
  styleUrl: './technical-specs.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TechnicalSpecsComponent {
  specifications = input.required<Specification[]>();
  title = input<string>('Technical Specifications');
}
