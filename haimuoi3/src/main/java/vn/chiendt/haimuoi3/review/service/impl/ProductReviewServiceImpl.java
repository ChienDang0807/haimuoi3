package vn.chiendt.haimuoi3.review.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.review.dto.request.CreateProductReviewRequest;
import vn.chiendt.haimuoi3.review.dto.response.ProductReviewResponse;
import vn.chiendt.haimuoi3.review.dto.response.ProductReviewStatsResponse;
import vn.chiendt.haimuoi3.review.model.mongo.ProductReviewEntity;
import vn.chiendt.haimuoi3.review.repository.ProductReviewRepository;
import vn.chiendt.haimuoi3.review.service.ProductReviewService;
import vn.chiendt.haimuoi3.review.validator.ProductReviewBusinessValidator;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final ProductReviewRepository productReviewRepository;
    private final ProductRepository productRepository;
    private final ProductReviewBusinessValidator productReviewBusinessValidator;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductReviewResponse> findByProductId(String productId, Pageable pageable) {
        return productReviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductReviewStatsResponse getStats(String productId) {
        List<ProductReviewEntity> all = productReviewRepository.findByProductId(productId);
        if (all.isEmpty()) {
            return ProductReviewStatsResponse.builder()
                    .averageRating(0.0)
                    .totalReviews(0)
                    .distribution(ProductReviewStatsResponse.emptyDistribution())
                    .build();
        }

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }

        double sum = 0;
        for (ProductReviewEntity e : all) {
            int r = Math.max(1, Math.min(5, e.getRating()));
            sum += r;
            distribution.merge(r, 1L, Long::sum);
        }

        double avg = sum / all.size();
        return ProductReviewStatsResponse.builder()
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .totalReviews(all.size())
                .distribution(distribution)
                .build();
    }

    @Override
    @Transactional
    public ProductReviewResponse create(String productId, CreateProductReviewRequest request, UserEntity currentUser) {
        productReviewBusinessValidator.validateReviewer(currentUser != null ? currentUser.getId() : null);
        productReviewBusinessValidator.validateCreate(request);

        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        if (productReviewRepository.existsByProductIdAndUserId(productId, currentUser.getId())) {
            throw new IllegalArgumentException("User already reviewed this product");
        }

        ProductReviewEntity entity = ProductReviewEntity.builder()
                .productId(productId)
                .userId(currentUser.getId())
                .rating(request.getRating())
                .comment(request.getComment())
                .authorDisplayName(currentUser.getFullName())
                .authorAvatarUrl(currentUser.getAvatarUrl())
                .createdAt(Instant.now())
                .build();

        return toResponse(productReviewRepository.save(entity));
    }

    private ProductReviewResponse toResponse(ProductReviewEntity e) {
        String dateStr = e.getCreatedAt() != null
                ? ISO_DATE.format(e.getCreatedAt().atOffset(ZoneOffset.UTC))
                : "";
        return ProductReviewResponse.builder()
                .id(e.getId())
                .userName(e.getAuthorDisplayName() != null ? e.getAuthorDisplayName() : "Customer")
                .userAvatar(e.getAuthorAvatarUrl())
                .rating(e.getRating())
                .comment(e.getComment() != null ? e.getComment() : "")
                .date(dateStr)
                .verified(Boolean.FALSE)
                .build();
    }
}
