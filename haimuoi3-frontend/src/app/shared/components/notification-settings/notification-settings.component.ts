import { Component, inject, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserRole } from '../../../core/constants/user-role';

@Component({
  selector: 'app-notification-settings',
  standalone: true,
  imports: [CommonModule, MatSlideToggleModule],
  templateUrl: './notification-settings.component.html',
  styleUrl: './notification-settings.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationSettingsComponent {
  private notificationService = inject(NotificationService);
  private authService = inject(AuthService);

  readonly soundEnabled = this.notificationService.soundEnabled;

  onSoundToggle(checked: boolean): void {
    this.notificationService.setSoundEnabled(checked);
    if (checked) {
      this.notificationService.playSoundPreview();
    }
  }

  readonly UserRole = UserRole;
}
