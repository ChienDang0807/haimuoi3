import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from '../../../shared/layout/header/header.component';
import { AccountSidebarComponent } from '../account-sidebar/account-sidebar.component';
import { NotificationSettingsComponent } from '../../../shared/components/notification-settings/notification-settings.component';

@Component({
  selector: 'app-account-settings-page',
  standalone: true,
  imports: [CommonModule, HeaderComponent, AccountSidebarComponent, NotificationSettingsComponent],
  templateUrl: './account-settings-page.component.html',
  styleUrl: './account-settings-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountSettingsPageComponent {}
