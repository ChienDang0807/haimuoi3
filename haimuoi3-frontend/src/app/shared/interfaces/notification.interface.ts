export type NotificationType =
  | 'ORDER_CREATED'
  | 'ORDER_UPDATED'
  | 'ORDER_CANCELLED'
  | 'ORDER_CONFIRMED'
  | 'ORDER_PAID'
  | 'ORDER_PAYMENT_FAILED'
  | 'ORDER_SHIPPING'
  | 'ORDER_DELIVERED';

export interface NotificationDTO {
  id: string;
  type: NotificationType;
  recipientId: number;
  recipientRole: string;
  payload: Record<string, unknown>;
  timestamp: string;
  read: boolean;
}
