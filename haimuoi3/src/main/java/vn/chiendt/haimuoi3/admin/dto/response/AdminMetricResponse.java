package vn.chiendt.haimuoi3.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMetricResponse {
    private String label;
    private String value;
    private String icon;
    private String trendDirection;
    private String trendValue;
}
