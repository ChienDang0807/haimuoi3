export interface Policy {
  id: string;
  title: string;
  /**
   * Material Symbols icon name (SSR-safe).
   * Example: 'swap_horiz', 'local_shipping', 'verified_user'
   */
  icon: string;
  items?: PolicyItem[];
  description?: string;
  isHighlighted?: boolean;
}

export interface PolicyItem {
  label: string;
  description: string;
}
