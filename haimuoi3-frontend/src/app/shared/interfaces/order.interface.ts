/** Line item trên đơn (GET order detail) */
export interface OrderItemResponse {
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

/** Chi tiết / dòng đơn (GET orders — khách hoặc shop owner) */
export interface OrderDetail {
  id: number;
  /** Có trên response shop; dùng cho bảng admin shop orders */
  shopId?: number;
  status: string;
  paymentMethod: string;
  shippingAddress: string;
  totalAmount: number;
  customerName: string;
  items?: OrderItemResponse[];
  createdAt: string;
}

/** Dòng trong body POST tạo đơn */
export interface CreateOrderLineRequest {
  productId: string;
  productName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

/** Body POST /orders — customerId lấy từ JWT, không gửi trong body */
export interface CreateOrderRequest {
  shopId: number;
  customerName: string;
  totalAmount: number;
  paymentMethod: string;
  shippingAddress: string;
  items: CreateOrderLineRequest[];
}

/** Phần tối thiểu trả về sau tạo đơn */
export interface OrderCreateResponse {
  id: number;
  status: string;
  paymentMethod: string;
  shopId?: number;
  totalAmount?: number;
}

/** POST /orders/checkout — body */
export interface CheckoutOrderRequest {
  cartId: string;
  customerName: string;
  shippingAddress: string;
  paymentMethod: string;
}

/** Response sau checkout đa shop */
export interface CheckoutBatchResponse {
  checkoutBatchId: string;
  orders: OrderCreateResponse[];
}

/** POST Stripe checkout session */
export interface StripeCheckoutSessionResponse {
  checkoutUrl: string;
}
