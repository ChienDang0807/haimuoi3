import { NotificationDTO, NotificationType } from '../../shared/interfaces';

export function getNotificationTitle(dto: NotificationDTO): string {
  const orderId = dto.payload['orderId'];
  const idStr = typeof orderId === 'number' ? String(orderId) : '?';

  const map: Record<NotificationType, string> = {
    ORDER_CREATED: `Đơn hàng mới #${idStr}`,
    ORDER_UPDATED: `Cập nhật đơn hàng #${idStr}`,
    ORDER_CANCELLED: `Đơn hàng #${idStr} đã bị hủy`,
    ORDER_CONFIRMED: `Đơn hàng #${idStr} đã được xác nhận`,
    ORDER_PAID: `Đã nhận thanh toán cho đơn #${idStr}`,
    ORDER_PAYMENT_FAILED: `Thanh toán thất bại cho đơn #${idStr}`,
    ORDER_SHIPPING: `Đơn hàng #${idStr} đang giao`,
    ORDER_DELIVERED: `Đơn hàng #${idStr} đã giao thành công`,
  };

  return map[dto.type] ?? 'Thông báo hệ thống';
}