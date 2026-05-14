export interface UserAction {
  type: 'shipping' | 'wishlist' | 'cart' | 'notifications' | 'profile';
  icon: string;
  count?: number;
  hasNotification?: boolean;
  title?: string;
}

export interface NavItem {
  label: string;
  route: string;
  isHighlighted?: boolean;
}
