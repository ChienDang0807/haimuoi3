/** Trạng thái đơn từ BE (OrderStatus enum, string). */
export type OrderStatusCode = string;

export function orderStatusLabel(status: OrderStatusCode): string {
  switch (status) {
    case 'READY_TO_SHIP':
      return 'Preparing Shipment';
    case 'SHIPPING':
      return 'In Transit';
    case 'DELIVERED':
      return 'Delivered';
    case 'CANCELLED':
      return 'Cancelled';
    case 'PAYMENT_FAILED':
      return 'Payment Failed';
    case 'PENDING_PAYMENT':
      return 'Pending Payment';
    case 'PAID':
    case 'CONFIRMED':
    case 'PENDING':
      return 'Processing';
    default:
      return status || 'Unknown';
  }
}

/** Lớp badge Tailwind (nền + chữ) theo mock Order Tracking */
export function orderStatusBadgeClass(status: OrderStatusCode): string {
  switch (status) {
    case 'READY_TO_SHIP':
      return 'bg-blue-100 text-blue-700';
    case 'SHIPPING':
      return 'bg-primary/10 text-primary';
    case 'DELIVERED':
      return 'bg-green-100 text-green-700';
    case 'CANCELLED':
    case 'PAYMENT_FAILED':
      return 'bg-error-container text-error';
    case 'PENDING_PAYMENT':
      return 'bg-tertiary-container text-on-tertiary-container';
    default:
      return 'bg-surface-container-highest text-secondary';
  }
}

export function isOrderActiveShipment(status: OrderStatusCode): boolean {
  return ['READY_TO_SHIP', 'SHIPPING', 'PENDING_PAYMENT', 'PENDING', 'CONFIRMED', 'PAID'].includes(status);
}

export function isOrderCompleted(status: OrderStatusCode): boolean {
  return status === 'DELIVERED';
}

/** Khop BE: chi PENDING / CONFIRMED (khong cho huy khi thanh toan online hoac da PAID — tranh refund). */
const CUSTOMER_CANCELLABLE = new Set<OrderStatusCode>(['PENDING', 'CONFIRMED']);

export function canCustomerCancelOrder(status: OrderStatusCode): boolean {
  return CUSTOMER_CANCELLABLE.has(status);
}

export function canCustomerConfirmDelivered(status: OrderStatusCode): boolean {
  return status === 'SHIPPING';
}
