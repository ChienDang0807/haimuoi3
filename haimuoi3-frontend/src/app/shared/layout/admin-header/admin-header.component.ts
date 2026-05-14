import { Component, input, output, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationBellComponent } from '../../../shared/components/notification-bell/notification-bell.component';

@Component({
  selector: 'app-admin-header',
  standalone: true,
  imports: [CommonModule, NotificationBellComponent],
  templateUrl: './admin-header.component.html',
  styleUrl: './admin-header.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminHeaderComponent {
  pageTitle = input<string>();
  searchPlaceholder = input<string>('Quick Search Components...');
  
  searchChange = output<string>();
  profileClick = output<void>();

  onSearchInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchChange.emit(value);
  }

  onProfileClick(): void {
    this.profileClick.emit();
  }
}
