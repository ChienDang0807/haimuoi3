package vn.chiendt.haimuoi3.review.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductReviewRequest {
    private Integer rating;
    private String comment;
}
