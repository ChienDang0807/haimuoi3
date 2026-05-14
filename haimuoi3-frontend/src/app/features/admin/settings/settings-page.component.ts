import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminSidebarComponent } from '../../../shared/layout/admin-sidebar/admin-sidebar.component';
import { AdminHeaderComponent } from '../../../shared/layout/admin-header/admin-header.component';
import { NotificationSettingsComponent } from '../../../shared/components/notification-settings/notification-settings.component';

@Component({
  selector: 'app-settings-page',
  standalone: true,
  imports: [CommonModule, AdminSidebarComponent, AdminHeaderComponent, NotificationSettingsComponent],
  templateUrl: './settings-page.component.html',
  styleUrl: './settings-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SettingsPageComponent {
  
}
