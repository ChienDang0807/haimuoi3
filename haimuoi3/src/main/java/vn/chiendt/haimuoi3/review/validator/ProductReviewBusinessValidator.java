package vn.chiendt.haimuoi3.review.validator;

import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.review.dto.request.CreateProductReviewRequest;

@Component
public class ProductReviewBusinessValidator {

    public void validateCreate(CreateProductReviewRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create product review request must not be null");
        }
        if (request.getRating() == null) {
            throw new IllegalArgumentException("rating must not be null");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
        if (request.getComment() != null && request.getComment().length() > Constants.Review.MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("comment must be less than or equal to 1000 characters");
        }
    }

    public void validateReviewer(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("reviewer context is invalid");
        }
    }
}
