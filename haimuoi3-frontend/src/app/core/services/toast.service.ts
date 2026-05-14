import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * Toast notification service using Angular Material Snackbar
 * Provides success, error, and info notifications with auto-dismiss
 */
@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private snackBar = inject(MatSnackBar);

  /**
   * Show success toast (green, 3s duration)
   */
  success(message: string): void {
    this.snackBar.open(message, '✓', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
      panelClass: ['success-toast']
    });
  }

  /**
   * Show error toast (red, 5s duration)
   */
  error(message: string): void {
    this.snackBar.open(message, '✕', {
      duration: 5000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
      panelClass: ['error-toast']
    });
  }

  /**
   * Show info toast (blue, 3s duration)
   */
  info(message: string): void {
    this.snackBar.open(message, 'ℹ', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
      panelClass: ['info-toast']
    });
  }

  /**
   * Show notification toast with action button
   */
  showNotificationToast(options: {
    title: string;
    actionLabel?: string;
    onAction: () => void;
    durationMs?: number;
  }): void {
    const ref = this.snackBar.open(options.title, options.actionLabel ?? 'Xem', {
      duration: options.durationMs ?? 5000,
      horizontalPosition: 'right',
      verticalPosition: 'top',
      panelClass: ['notification-toast'],
    });
    ref.onAction().subscribe(() => options.onAction());
  }
}
