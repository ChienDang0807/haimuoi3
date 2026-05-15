import { Component, input, output, ChangeDetectionStrategy, HostListener, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationBellComponent } from '../../../shared/components/notification-bell/notification-bell.component';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-header',
  standalone: true,
  imports: [CommonModule, NotificationBellComponent],
  templateUrl: './admin-header.component.html',
  styleUrl: './admin-header.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminHeaderComponent {
  private readonly authService = inject(AuthService);

  pageTitle = input<string>();
  searchPlaceholder = input<string>('Quick Search Components...');

  searchChange = output<string>();
  profileClick = output<void>();

  readonly isAccountMenuOpen = signal(false);
  readonly displayName = computed(() => this.authService.currentUser()?.fullName || 'Admin User');
  readonly displayEmail = computed(() => this.authService.currentUser()?.email || '');
  readonly shopLabel = computed(() => this.authService.currentUser()?.shopName || 'Store Admin');
  readonly accountInitial = computed(() => {
    const name = this.displayName().trim();
    return name ? name.charAt(0).toUpperCase() : 'A';
  });

  onSearchInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.searchChange.emit(value);
  }

  onAccountButtonClick(event: MouseEvent): void {
    event.stopPropagation();
    this.isAccountMenuOpen.update(open => !open);
    this.profileClick.emit();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.account-menu-root')) {
      this.isAccountMenuOpen.set(false);
    }
  }

  onLogoutFromMenu(): void {
    this.isAccountMenuOpen.set(false);
    this.authService.logout();
  }
}
