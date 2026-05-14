package vn.chiendt.haimuoi3.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminTrendPointResponse {
    private String label;
    private BigDecimal revenue;
    private long orderCount;
}
