package vn.chiendt.haimuoi3.review.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.review.dto.request.CreateProductReviewRequest;
import vn.chiendt.haimuoi3.review.dto.response.ProductReviewResponse;
import vn.chiendt.haimuoi3.review.dto.response.ProductReviewStatsResponse;
import vn.chiendt.haimuoi3.review.service.ProductReviewService;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    @GetMapping("/{productId}/reviews")
    public ApiResponse<Page<ProductReviewResponse>> listReviews(
            @PathVariable String productId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProductReviewResponse> page = productReviewService.findByProductId(productId, pageable);
        return ApiResponse.success(page, "Fetched product reviews");
    }

    @GetMapping("/{productId}/reviews/stats")
    public ApiResponse<ProductReviewStatsResponse> reviewStats(@PathVariable String productId) {
        ProductReviewStatsResponse stats = productReviewService.getStats(productId);
        return ApiResponse.success(stats, "Fetched product review stats");
    }

    @PostMapping("/{productId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductReviewResponse> createReview(
            @PathVariable String productId,
            @RequestBody CreateProductReviewRequest request,
            @AuthenticationPrincipal UserEntity currentUser
    ) {
        ProductReviewResponse response = productReviewService.create(productId, request, currentUser);
        return ApiResponse.success(response, "Product review created");
    }
}
