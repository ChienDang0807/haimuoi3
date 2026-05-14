package vn.chiendt.haimuoi3.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.chiendt.haimuoi3.review.dto.request.CreateProductReviewRequest;
import vn.chiendt.haimuoi3.review.dto.response.ProductReviewResponse;
import vn.chiendt.haimuoi3.review.dto.response.ProductReviewStatsResponse;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

public interface ProductReviewService {

    Page<ProductReviewResponse> findByProductId(String productId, Pageable pageable);

    ProductReviewStatsResponse getStats(String productId);

    ProductReviewResponse create(String productId, CreateProductReviewRequest request, UserEntity currentUser);
}
