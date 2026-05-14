import { NotificationDTO } from '../../shared/interfaces';

export function getNotificationRoute(dto: NotificationDTO, role: string): string[] {
  const orderId = dto.payload['orderId'];
  if (typeof orderId === 'number' && orderId > 0) {
    if (role === 'CUSTOMER') {
      return ['/account/orders', String(orderId)];
    } else if (role === 'SHOP_OWNER') {
      return ['/admin/orders', String(orderId)];
    }
  }
  // Fallback
  if (role === 'CUSTOMER') {
    return ['/account/orders'];
  } else if (role === 'SHOP_OWNER') {
    return ['/admin/orders'];
  }
  return ['/'];
}