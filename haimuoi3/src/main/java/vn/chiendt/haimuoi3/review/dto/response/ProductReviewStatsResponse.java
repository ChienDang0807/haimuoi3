package vn.chiendt.haimuoi3.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewStatsResponse {

    private double averageRating;
    private long totalReviews;
    private Map<Integer, Long> distribution;

    public static Map<Integer, Long> emptyDistribution() {
        Map<Integer, Long> map = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            map.put(i, 0L);
        }
        return map;
    }
}
