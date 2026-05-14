import { Injectable, inject, signal, computed, effect, PLATFORM_ID, ElementRef, AfterViewInit } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { Client, StompConfig, StompSubscription } from '@stomp/stompjs';
import { Observable, Subject, debounceTime, filter, take } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';
import { ToastService } from './toast.service';
import { NotificationDTO, NotificationType } from '../../shared/interfaces';
import { getNotificationTitle } from '../../shared/utils/notification-title.util';
import { getNotificationRoute } from '../../shared/utils/notification-navigation.util';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private platformId = inject(PLATFORM_ID);
  private router = inject(Router);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  // SSR guard
  private get isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  // Private writable signals
  private _notifications = signal<NotificationDTO[]>([]);
  private _unreadCount = signal<number>(0);
  private _connectionStatus = signal<'CONNECTING' | 'CONNECTED' | 'DISCONNECTED'>('DISCONNECTED');
  private _isPanelOpen = signal<boolean>(false);
  private _soundEnabled = signal<boolean>(false);

  // Public readonly signals
  readonly notifications = this._notifications.asReadonly();
  readonly unreadCount = this._unreadCount.asReadonly();
  readonly connectionStatus = this._connectionStatus.asReadonly();
  readonly isPanelOpen = this._isPanelOpen.asReadonly();
  readonly soundEnabled = this._soundEnabled.asReadonly();

  // Private properties
  private client: Client | null = null;
  private stompActivationInProgress = false;
  private customerSubscription: StompSubscription | undefined | null = null;
  private shopSubscription: StompSubscription | undefined | null = null;
  private lastSoundPlayedAt = 0;
  private toastSubject = new Subject<NotificationDTO>();
  private toastDebounce$ = this.toastSubject.pipe(
    debounceTime(500),
    filter(() => this.isBrowser && !this._isPanelOpen())
  );

  constructor() {
    // Load sound setting from localStorage
    this.loadSoundSetting();

    // Effect: listen to user changes to activate/deactivate STOMP
    effect(() => {
      const user = this.authService.currentUser();
      if (user && this.isBrowser) {
        this.activateStompClient();
      } else if (!user && this.client) {
        this.deactivateStompClient();
      }
    });

    // Effect: listen to shopId changes to subscribe shop topic
    effect(() => {
      const user = this.authService.currentUser();
      if (user?.role === 'SHOP_OWNER' && user.shopId && this.client?.active && !this.shopSubscription) {
        this.subscribeShopTopic(user.shopId);
      }
    });

    // Effect: debounce toast
    this.toastDebounce$.subscribe(dto => {
      this.showNotificationToast(dto);
    });
  }

  // 4.1 — SSR guard helper (already in place above)

  // 4.2 — STOMP lifecycle
  private activateStompClient(): void {
    if (!this.isBrowser || this.client?.active || this.stompActivationInProgress) {
      return;
    }

    const token = this.authService.getToken();
    if (!token) {
      return;
    }

    const wsUrl = environment.apiUrl.replace(/\/api$/, '') + '/ws';
    this.stompActivationInProgress = true;
    this._connectionStatus.set('CONNECTING');

    void import('sockjs-client')
      .then(({ default: SockJS }) => {
        if (!this.isBrowser || this.client?.active) {
          return;
        }

        const config: StompConfig = {
          webSocketFactory: () => new SockJS(wsUrl),
          connectHeaders: {
            Authorization: `Bearer ${token}`,
          },
          reconnectDelay: 1000,
          onConnect: () => {
            this._connectionStatus.set('CONNECTED');
            this.subscribeToChannel();
          },
          onDisconnect: () => {
            this._connectionStatus.set('DISCONNECTED');
          },
          onStompError: frame => {
            const headers = frame.headers;
            if (headers?.['message']?.includes('401') || headers?.['message']?.includes('403')) {
              this.deactivateStompClient();
              this.authService.logout();
            }
          },
        };

        this.client = new Client(config);
        this.client.activate();
      })
      .catch(() => {
        this._connectionStatus.set('DISCONNECTED');
      })
      .finally(() => {
        this.stompActivationInProgress = false;
      });
  }

  private deactivateStompClient(): void {
    if (!this.client) return;

    this.customerSubscription?.unsubscribe();
    this.shopSubscription?.unsubscribe();
    this.client.deactivate();
    this.client = null;
    this._notifications.set([]);
    this._unreadCount.set(0);
    this._connectionStatus.set('DISCONNECTED');
  }

  // 4.3 — Subscribe the correct channel based on role
  private subscribeToChannel(): void {
    const user = this.authService.currentUser();
    if (!user) return;

    if (user.role === 'CUSTOMER') {
      this.subscribeCustomerTopic(user.userId);
    } else if (user.role === 'SHOP_OWNER' && user.shopId) {
      this.subscribeShopTopic(user.shopId);
    }
  }

  private subscribeCustomerTopic(userId: number): void {
    if (this.customerSubscription) return;
    this.customerSubscription = this.client?.subscribe(`/queue/user/${userId}/notifications`, (msg) => {
      this.handleMessage(msg.body);
    });
  }

  private subscribeShopTopic(shopId: number): void {
    if (this.shopSubscription) return;
    this.shopSubscription = this.client?.subscribe(`/topic/shop/${shopId}/notifications`, (msg) => {
      this.handleMessage(msg.body);
    });
  }

  // 4.4 — Handle incoming STOMP MESSAGE
  private handleMessage(body: string): void {
    if (!this.isBrowser) return;

    // Validate size
    if (body.length > 64 * 1024) {
      console.warn('Notification payload exceeds 64KB, ignoring');
      return;
    }

    // Parse and validate
    let dto: NotificationDTO;
    try {
      dto = JSON.parse(body);
    } catch {
      console.warn('Invalid JSON in notification message');
      return;
    }

    if (!dto.id || !dto.type || !dto.timestamp) {
      console.warn('Malformed notification DTO', dto);
      return;
    }

    // Prepend to store (max 100)
    this._notifications.update(list => {
      const newList = [dto, ...list];
      return newList.slice(0, 100);
    });

    // Increment unread if not read
    if (!dto.read) {
      this._unreadCount.update(c => c + 1);
    }

    // Debounce toast
    this.toastSubject.next(dto);

    // Play sound if conditions met
    this.playNotificationSound(dto);
  }

  // 4.5 — Toast when panel is closed
  private showNotificationToast(dto: NotificationDTO): void {
    if (this._isPanelOpen()) return;

    const title = getNotificationTitle(dto);
    this.toastService.showNotificationToast({
      title,
      actionLabel: 'Xem',
      onAction: () => {
        this.markAsRead(dto.id);
        const route = getNotificationRoute(dto, this.authService.currentUser()?.role || '');
        this.router.navigate(route);
      },
    });
  }

  // 4.6 — Sound with throttle
  private playNotificationSound(dto: NotificationDTO): void {
    if (!this.isBrowser) return;

    const user = this.authService.currentUser();
    if (!user) return;

    // Check conditions
    const alreadyInStore = this._notifications().some(n => n.id === dto.id);
    if (alreadyInStore) return;
    if (dto.read) return;
    if (!this._soundEnabled()) return;
    if (document.visibilityState !== 'visible') return;
    if (this._isPanelOpen()) return;

    // Throttle 2500ms
    const now = Date.now();
    if (now - this.lastSoundPlayedAt < 2500) return;

    // Play sound
    try {
      const audio = new Audio('assets/sounds/notification.mp3');
      audio.volume = 0.4;
      audio.loop = false;
      audio.play().catch(() => {});
      this.lastSoundPlayedAt = now;
    } catch {
      // Silent catch
    }
  }

  // 4.7 — Sound setting persistence
  private loadSoundSetting(): void {
    if (!this.isBrowser) return;
    const user = this.authService.currentUser();
    if (!user) return;

    try {
      const raw = localStorage.getItem(`haimuoi3.notifications.sound.${user.userId}`);
      this._soundEnabled.set(raw === 'true');
    } catch {
      this._soundEnabled.set(false);
    }
  }

  setSoundEnabled(enabled: boolean): void {
    if (!this.isBrowser) return;
    const user = this.authService.currentUser();
    if (!user) return;

    localStorage.setItem(`haimuoi3.notifications.sound.${user.userId}`, enabled ? 'true' : 'false');
    this._soundEnabled.set(enabled);
  }

  playSoundPreview(): void {
    if (!this.isBrowser) return;
    try {
      const audio = new Audio('assets/sounds/notification.mp3');
      audio.volume = 0.4;
      audio.loop = false;
      audio.play().catch(() => {});
    } catch {
      // Silent catch
    }
  }

  // 4.8 — Public methods
  openPanel(): void {
    this._isPanelOpen.set(true);
    // Mark all displayed items as read
    this._notifications.update(list => list.map(n => ({ ...n, read: true })));
    this._unreadCount.set(0);
  }

  closePanel(): void {
    this._isPanelOpen.set(false);
  }

  markAsRead(id: string): void {
    this._notifications.update(list =>
      list.map(n => (n.id === id ? { ...n, read: true } : n))
    );
    this._unreadCount.update(c => Math.max(0, c - 1));
  }
}
