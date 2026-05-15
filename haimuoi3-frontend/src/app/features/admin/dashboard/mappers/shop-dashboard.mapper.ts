import type { EChartsOption } from 'echarts';
import {
  ShopDashboardKpiCardDto,
  ShopDashboardKpiTrendDto,
  ShopDashboardOrderStatusSliceDto,
  ShopDashboardRecentOrderDto,
  ShopDashboardRevenuePointDto,
  ShopDashboardTopProductDto,
  ShopOwnerDashboardResponse,
} from '../../../../shared/interfaces';
import {
  DashboardKpiCardVm,
  DashboardPageVm,
  DashboardRecentOrderVm,
  DashboardRevenuePointVm,
  DashboardTopProductVm,
} from '../models/shop-dashboard.models';

const DEFAULT_HEADER_SUBTITLE = 'Tổng quan 7 ngày gần nhất';
const DEFAULT_SHOP_NAME = 'Cửa hàng của bạn';

const AVATAR_GRADIENTS = [
  'from-emerald-400 to-emerald-600',
  'from-blue-400 to-blue-600',
  'from-pink-400 to-pink-600',
  'from-purple-400 to-purple-600',
  'from-orange-400 to-orange-600',
  'from-red-400 to-red-600',
];

const vndFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0,
});

function toTrendDirection(value: string | undefined): 'up' | 'down' | 'neutral' {
  if (value === 'up' || value === 'down' || value === 'neutral') {
    return value;
  }
  return 'neutral';
}

function toProductTrendDirection(value: string | undefined): 'up' | 'down' | 'neutral' {
  if (value === 'up' || value === 'down') {
    return value;
  }
  return 'neutral';
}

function trendIsPositive(trend: ShopDashboardKpiTrendDto | undefined): boolean {
  if (!trend) {
    return true;
  }
  if (typeof trend.isPositive === 'boolean') {
    return trend.isPositive;
  }
  if (typeof trend.positive === 'boolean') {
    return trend.positive;
  }
  return true;
}

function deriveInitials(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) {
    return '?';
  }
  if (parts.length === 1) {
    return parts[0].slice(0, 2).toUpperCase();
  }
  return `${parts[0][0] ?? ''}${parts[parts.length - 1][0] ?? ''}`.toUpperCase();
}

function deriveAvatarGradient(name: string): string {
  let hash = 0;
  for (let i = 0; i < name.length; i += 1) {
    hash = (hash + name.charCodeAt(i)) % AVATAR_GRADIENTS.length;
  }
  return AVATAR_GRADIENTS[hash] ?? AVATAR_GRADIENTS[0];
}

function mapRevenuePoint(point: ShopDashboardRevenuePointDto): DashboardRevenuePointVm {
  return {
    label: point.label ?? '',
    revenue: point.revenue ?? 0,
    orderCount: point.orderCount,
  };
}

function mapKpiCard(card: ShopDashboardKpiCardDto): DashboardKpiCardVm {
  const trend = card.trend;
  return {
    label: card.label ?? '',
    value: card.value ?? '—',
    icon: card.icon ?? 'insights',
    trend: trend
      ? {
          direction: toTrendDirection(trend.direction),
          value: trend.value ?? '',
          isPositive: trendIsPositive(trend),
        }
      : undefined,
  };
}

function mapRecentOrder(order: ShopDashboardRecentOrderDto): DashboardRecentOrderVm {
  const customerName = order.customerName ?? 'Khách';
  return {
    orderId: order.orderId ?? 0,
    displayOrderCode: order.displayOrderCode ?? `#${order.orderId ?? ''}`,
    customerName,
    customerInitials: order.customerInitials ?? deriveInitials(customerName),
    customerColor: order.customerColor ?? deriveAvatarGradient(customerName),
    itemCount: order.itemCount ?? 0,
    status: order.status ?? '',
    statusLabel: order.statusLabel ?? order.status ?? '',
    timeAgoLabel: order.timeAgoLabel ?? '—',
    totalAmount: order.totalAmount ?? 0,
  };
}

function mapTopProduct(product: ShopDashboardTopProductDto): DashboardTopProductVm {
  return {
    productId: product.productId ?? '',
    name: product.name ?? '—',
    imageUrl: product.imageUrl ?? '',
    salesCount: product.salesCount ?? 0,
    trendPercent: product.trendPercent ?? 0,
    trendDirection: toProductTrendDirection(product.trendDirection),
  };
}

export function formatDashboardCurrency(amount: number): string {
  return vndFormatter.format(amount);
}

export function mapShopDashboardResponse(response: ShopOwnerDashboardResponse | null | undefined): DashboardPageVm {
  const header = response?.header;
  return {
    header: {
      shopName: header?.shopName ?? DEFAULT_SHOP_NAME,
      subtitle: header?.subtitle ?? DEFAULT_HEADER_SUBTITLE,
    },
    metrics: (response?.kpiCards ?? []).map(mapKpiCard),
    revenueTrend: (response?.revenueTrend ?? []).map(mapRevenuePoint),
    revenueTrendPrevious: (response?.revenueTrendPrevious ?? []).map(mapRevenuePoint),
    recentOrders: (response?.recentOrders ?? []).map(mapRecentOrder),
    orderStatusBreakdown: (response?.orderStatusBreakdown ?? []).map(
      (slice: ShopDashboardOrderStatusSliceDto) => ({
        statusKey: slice.statusKey ?? '',
        label: slice.label ?? '',
        count: slice.count ?? 0,
        colorToken: slice.colorToken ?? 'primary',
      }),
    ),
    fulfillmentRatePercent: response?.fulfillmentRatePercent ?? 0,
    topProducts: (response?.topProducts ?? []).map(mapTopProduct),
    lowStockAlerts: (response?.lowStockAlerts ?? []).map(alert => ({
      productId: alert.productId ?? '',
      productName: alert.productName ?? '—',
      quantityOnHand: alert.quantityOnHand ?? 0,
    })),
    shopHealth: response?.shopHealth
      ? {
          connectionLabel: response.shopHealth.connectionLabel ?? '',
          lastSyncedAt: response.shopHealth.lastSyncedAt ?? '',
        }
      : null,
  };
}

export function buildDashboardChartOptions(
  current: DashboardRevenuePointVm[],
  previous: DashboardRevenuePointVm[],
): EChartsOption {
  const labels = current.map(point => point.label);
  return {
    tooltip: {
      trigger: 'axis',
      formatter: (params: unknown) => {
        if (!Array.isArray(params)) {
          return '';
        }
        return params
          .map(item => {
            const dataIndex = typeof item.dataIndex === 'number' ? item.dataIndex : 0;
            const value = typeof item.value === 'number' ? item.value : 0;
            const seriesName = typeof item.seriesName === 'string' ? item.seriesName : '';
            const orderCount = seriesName === 'Kỳ hiện tại' ? current[dataIndex]?.orderCount : undefined;
            const amount = formatDashboardCurrency(value);
            return orderCount != null
              ? `${seriesName}: ${amount} (${orderCount} đơn)`
              : `${seriesName}: ${amount}`;
          })
          .join('<br/>');
      },
    },
    grid: { left: 40, right: 24, top: 24, bottom: 32 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels,
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => formatDashboardCurrency(value),
      },
    },
    series: [
      {
        name: 'Kỳ hiện tại',
        type: 'line',
        smooth: true,
        data: current.map(point => point.revenue),
        lineStyle: { color: '#455768', width: 2 },
        itemStyle: { color: '#455768' },
      },
      ...(previous.length > 0
        ? [
            {
              name: 'Kỳ trước',
              type: 'line' as const,
              smooth: true,
              data: previous.map(point => point.revenue),
              lineStyle: { color: '#cbd5e1', width: 2, type: 'dashed' as const },
              itemStyle: { color: '#cbd5e1' },
            },
          ]
        : []),
    ],
  };
}

export function statusSliceColor(colorToken: string): string {
  switch (colorToken) {
    case 'primary':
      return '#455768';
    case 'success':
      return '#10b981';
    case 'warning':
      return '#f59e0b';
    case 'muted':
      return '#cbd5e1';
    default:
      return '#94a3b8';
  }
}
