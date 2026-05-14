import { Component, inject, signal, computed, ElementRef, HostListener, AfterViewInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatBadgeModule } from '@angular/material/badge';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserRole } from '../../../core/constants/user-role';
import { NotificationDTO } from '../../../shared/interfaces';
import { getNotificationTitle } from '../../../shared/utils/notification-title.util';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule, MatBadgeModule],
  templateUrl: './notification-bell.component.html',
  styleUrl: './notification-bell.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationBellComponent implements AfterViewInit {
  private router = inject(Router);
  private notificationService = inject(NotificationService);
  private authService = inject(AuthService);

  // Expose signals from NotificationService
  readonly notifications = this.notificationService.notifications;
  readonly unreadCount = this.notificationService.unreadCount;
  readonly connectionStatus = this.notificationService.connectionStatus;
  readonly isPanelOpen = this.notificationService.isPanelOpen;

  // Computed for disabled state
  readonly isShopOwnerWithoutShopId = computed(() => {
    const user = this.authService.currentUser();
    return user?.role === UserRole.SHOP_OWNER && !user?.shopId;
  });

  // Computed for aria-label
  readonly bellAriaLabel = computed(() => {
    const count = this.unreadCount();
    if (count > 0) {
      return `Thông báo, ${count} chưa đọc`;
    }
    return 'Thông báo';
  });

  // Computed for badge text
  readonly badgeText = computed(() => {
    const count = this.unreadCount();
    return count > 99 ? '99+' : String(count);
  });

  // Computed for disabled state
  readonly isDisabled = computed(() => {
    return this.isShopOwnerWithoutShopId() || this.connectionStatus() === 'DISCONNECTED';
  });

  // Computed for connection indicator
  readonly showPulsing = computed(() => this.connectionStatus() === 'CONNECTING');
  readonly showOffline = computed(() => this.connectionStatus() === 'DISCONNECTED');

  // Expose role for template
  readonly role = computed(() => this.authService.currentUser()?.role || '');

  ngAfterViewInit(): void {
    // Focus management handled by template
  }

  togglePanel(): void {
    if (this.isPanelOpen()) {
      this.notificationService.closePanel();
    } else {
      this.notificationService.openPanel();
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.notification-bell-root')) {
      this.notificationService.closePanel();
    }
  }

  onNotificationClick(dto: NotificationDTO): void {
    this.notificationService.markAsRead(dto.id);
    const role = this.authService.currentUser()?.role || '';
    this.router.navigate(this.getNotificationRoute(dto, role));
  }

  getNotificationTitle(dto: NotificationDTO): string {
    return getNotificationTitle(dto);
  }

  readonly UserRole = UserRole;

  private getNotificationRoute(dto: NotificationDTO, role: string): string[] {
    const orderId = dto.payload['orderId'];
    if (typeof orderId === 'number' && orderId > 0) {
      if (role === UserRole.CUSTOMER) {
        return ['/account/orders', String(orderId)];
      } else if (role === UserRole.SHOP_OWNER) {
        return ['/admin/orders', String(orderId)];
      }
    }
    if (role === UserRole.CUSTOMER) {
      return ['/account/orders'];
    } else if (role === UserRole.SHOP_OWNER) {
      return ['/admin/orders'];
    }
    return ['/'];
  }
}
