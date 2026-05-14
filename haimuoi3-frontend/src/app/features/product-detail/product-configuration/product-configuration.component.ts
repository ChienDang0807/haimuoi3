import { Component, input, output, signal, ChangeDetectionStrategy, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfigOption } from '../../../shared/interfaces';

@Component({
  selector: 'app-product-configuration',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-configuration.component.html',
  styleUrl: './product-configuration.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductConfigurationComponent {
  options = input.required<ConfigOption[]>();
  title = input<string>('Configuration');

  optionChange = output<ConfigOption>();

  selectedOptionId = signal<string>('');

  constructor() {
    effect(() => {
      const opts = this.options();
      if (opts.length === 0) {
        return;
      }
      const current = this.selectedOptionId();
      const stillValid = !!(current && opts.some(o => o.id === current));
      if (stillValid) {
        return;
      }
      const preferred = opts.find(o => o.selected)?.id ?? opts[0].id;
      this.selectedOptionId.set(preferred);
      const chosen = opts.find(o => o.id === preferred);
      if (chosen) {
        this.optionChange.emit(chosen);
      }
    });
  }

  selectOption(option: ConfigOption): void {
    this.selectedOptionId.set(option.id);
    this.optionChange.emit(option);
  }

  isSelected(optionId: string): boolean {
    return this.selectedOptionId() === optionId;
  }
}
