package vn.chiendt.haimuoi3.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewResponse {

    private String id;
    private String userName;
    private String userAvatar;
    private int rating;
    private String comment;
    private String date;
    private Boolean verified;
}
