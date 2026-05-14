export interface AdminStat {
  label: string;
  value: string | number;
  icon?: string;
  trend?: {
    direction: 'up' | 'down' | 'neutral';
    value: string;
  };
  backgroundColor?: string;
}
