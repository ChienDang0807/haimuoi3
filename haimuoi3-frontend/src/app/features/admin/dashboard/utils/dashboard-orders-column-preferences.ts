import { isPlatformBrowser } from '@angular/common';

import { DashboardOrderColumnKey } from '../models/shop-dashboard.models';

const STORAGE_PREFIX = 'haimuoi3.admin.dashboard.orders.columns';

export const DEFAULT_DASHBOARD_ORDER_COLUMNS: DashboardOrderColumnKey[] = [
  'orderId',
  'customer',
  'items',
  'status',
  'time',
  'amount',
  'action',
];

const REQUIRED_DASHBOARD_ORDER_COLUMNS: DashboardOrderColumnKey[] = ['orderId', 'action'];

function storageKey(userId: number | string): string {
  return `${STORAGE_PREFIX}.${userId}`;
}

function normalizeColumns(columns: DashboardOrderColumnKey[] | null | undefined): DashboardOrderColumnKey[] {
  const allowed = new Set(DEFAULT_DASHBOARD_ORDER_COLUMNS);
  const selected = new Set<DashboardOrderColumnKey>();

  for (const column of columns ?? []) {
    if (allowed.has(column)) {
      selected.add(column);
    }
  }

  for (const required of REQUIRED_DASHBOARD_ORDER_COLUMNS) {
    selected.add(required);
  }

  return DEFAULT_DASHBOARD_ORDER_COLUMNS.filter(column => selected.has(column));
}

export function loadVisibleDashboardOrderColumns(
  userId: number | string | null | undefined,
  platformId: object,
): DashboardOrderColumnKey[] {
  if (!userId || !isPlatformBrowser(platformId)) {
    return [...DEFAULT_DASHBOARD_ORDER_COLUMNS];
  }

  try {
    const raw = localStorage.getItem(storageKey(userId));
    if (!raw) {
      return [...DEFAULT_DASHBOARD_ORDER_COLUMNS];
    }
    const parsed = JSON.parse(raw) as DashboardOrderColumnKey[];
    return normalizeColumns(parsed);
  } catch {
    return [...DEFAULT_DASHBOARD_ORDER_COLUMNS];
  }
}

export function saveVisibleDashboardOrderColumns(
  userId: number | string | null | undefined,
  columns: DashboardOrderColumnKey[],
  platformId: object,
): DashboardOrderColumnKey[] {
  const normalized = normalizeColumns(columns);
  if (!userId || !isPlatformBrowser(platformId)) {
    return normalized;
  }

  localStorage.setItem(storageKey(userId), JSON.stringify(normalized));
  return normalized;
}

export function isDashboardOrderColumnVisible(
  columns: DashboardOrderColumnKey[],
  column: DashboardOrderColumnKey,
): boolean {
  return columns.includes(column);
}

export function toggleDashboardOrderColumn(
  columns: DashboardOrderColumnKey[],
  column: DashboardOrderColumnKey,
): DashboardOrderColumnKey[] {
  if (REQUIRED_DASHBOARD_ORDER_COLUMNS.includes(column)) {
    return normalizeColumns(columns);
  }

  if (columns.includes(column)) {
    return normalizeColumns(columns.filter(item => item !== column));
  }

  return normalizeColumns([...columns, column]);
}
