import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../shared/layout/header/header.component';
import { AccountSidebarComponent } from '../account-sidebar/account-sidebar.component';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-account-payment-methods-page',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent, AccountSidebarComponent],
  templateUrl: './account-payment-methods-page.component.html',
  styleUrl: './account-payment-methods-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountPaymentMethodsPageComponent {
  readonly authService = inject(AuthService);
}
