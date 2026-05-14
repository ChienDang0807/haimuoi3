import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ProfileService } from '../../../core/services/profile.service';

@Component({
  selector: 'app-account-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './account-sidebar.component.html',
  styleUrl: './account-sidebar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountSidebarComponent {
  readonly authService = inject(AuthService);
  private readonly profileService = inject(ProfileService);

  readonly profile = this.profileService.currentProfile;
  readonly avatarLoadError = signal(false);
  readonly initial = computed(() => {
    const name = this.profile()?.fullName?.trim();
    if (name && name.length > 0) {
      return name.charAt(0).toUpperCase();
    }
    const email = this.profile()?.email?.trim();
    if (email && email.length > 0) {
      return email.charAt(0).toUpperCase();
    }
    return 'U';
  });
  readonly accountLabel = computed(() => {
    const role = this.profile() ? 'Customer Account' : 'Account';
    return role;
  });

  constructor() {
    effect(() => {
      this.profile();
      this.avatarLoadError.set(false);
    });

    if (this.authService.isLoggedIn() && !this.profileService.currentProfile()) {
      this.profileService.getProfile().subscribe({ error: () => {} });
    }
  }

  onAvatarError(): void {
    this.avatarLoadError.set(true);
  }

  logout(): void {
    this.profileService.clear();
    this.authService.logout();
  }
}
