import { Component, input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface AlertCardConfig {
  title: string;
  description: string;
  icon: string;
  type: 'error' | 'warning' | 'success' | 'info' | 'primary';
  actionText?: string;
  actionLink?: string;
}

@Component({
  selector: 'app-alert-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './alert-card.component.html',
  styleUrl: './alert-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AlertCardComponent {
  config = input.required<AlertCardConfig>();

  get cardClasses(): string {
    const baseClasses = 'p-8 rounded-xl flex flex-col justify-between aspect-video md:aspect-auto';
    
    switch (this.config().type) {
      case 'error':
        return `${baseClasses} bg-error-container border-l-8 border-error`;
      case 'warning':
        return `${baseClasses} bg-amber-50 border-l-8 border-amber-500`;
      case 'success':
        return `${baseClasses} bg-emerald-50 border-l-8 border-emerald-500`;
      case 'primary':
        return `${baseClasses} bg-primary text-on-primary shadow-2xl`;
      default:
        return `${baseClasses} bg-surface-container border-l-8 border-primary`;
    }
  }

  get iconClasses(): string {
    switch (this.config().type) {
      case 'error':
        return 'text-error';
      case 'warning':
        return 'text-amber-600';
      case 'success':
        return 'text-emerald-600';
      case 'primary':
        return 'opacity-80';
      default:
        return 'text-primary';
    }
  }

  get textClasses(): string {
    return this.config().type === 'primary' ? 'text-on-primary' : 'text-on-surface';
  }
}
