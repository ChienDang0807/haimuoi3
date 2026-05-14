import { Component, ChangeDetectionStrategy, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { EChartsOption } from 'echarts';
import { NgxEchartsDirective } from 'ngx-echarts';
import { AdminSidebarComponent } from '../../../shared/layout/admin-sidebar/admin-sidebar.component';
import { AdminHeaderComponent } from '../../../shared/layout/admin-header/admin-header.component';
import { AdminDataService, AdminDashboardMetrics, AdminModerationProduct } from '../../../core/services/admin-data.service';
import { AdminStat } from '../../../shared/interfaces/admin';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, NgxEchartsDirective, AdminSidebarComponent, AdminHeaderComponent],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardPageComponent {
  private readonly adminDataService = inject(AdminDataService);
  private readonly platformId = inject(PLATFORM_ID);
  readonly isBrowser = isPlatformBrowser(this.platformId);

  readonly metrics = signal<AdminStat[]>([]);
  readonly moderationProducts = signal<AdminModerationProduct[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly chartOptions = signal<EChartsOption>(this.emptyChartOptions());
  readonly heroMetric = computed<AdminStat>(() => this.metrics()[0] ?? { label: 'Total Revenue', value: '$0' });
  readonly secondaryMetrics = computed(() => this.metrics().slice(1, 4));

  constructor() {
    if (this.isBrowser) {
      this.loadDashboard();
    } else {
      this.isLoading.set(false);
    }
  }

  private loadDashboard(): void {
    this.adminDataService.getDashboardMetrics().subscribe({
      next: data => {
        this.applyDashboardMetrics(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Unable to load admin dashboard metrics.');
        this.isLoading.set(false);
      }
    });

    this.adminDataService.searchModerationProducts('', '', 0, 5).subscribe({
      next: page => this.moderationProducts.set(page.content),
      error: () => this.moderationProducts.set([])
    });
  }

  private applyDashboardMetrics(data: AdminDashboardMetrics): void {
    this.metrics.set(data.metrics);
    this.chartOptions.set({
      tooltip: { trigger: 'axis' },
      grid: { left: 8, right: 8, top: 16, bottom: 8, containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: data.revenueTrend.map(point => point.label),
      },
      yAxis: { type: 'value' },
      series: [
        {
          name: 'Revenue',
          type: 'line',
          smooth: true,
          areaStyle: {},
          data: data.revenueTrend.map(point => point.revenue),
        }
      ],
    });
  }

  private emptyChartOptions(): EChartsOption {
    return {
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: [{ type: 'line', smooth: true, data: [] }],
    };
  }
}
