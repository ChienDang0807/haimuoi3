package vn.chiendt.haimuoi3.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardMetricsResponse {
    private List<AdminMetricResponse> metrics;
    private List<AdminTrendPointResponse> revenueTrend;
}
