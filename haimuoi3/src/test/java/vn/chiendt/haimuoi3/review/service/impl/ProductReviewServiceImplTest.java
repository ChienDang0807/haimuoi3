package vn.chiendt.haimuoi3.review.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.review.dto.request.CreateProductReviewRequest;
import vn.chiendt.haimuoi3.review.dto.response.ProductReviewResponse;
import vn.chiendt.haimuoi3.review.dto.response.ProductReviewStatsResponse;
import vn.chiendt.haimuoi3.review.model.mongo.ProductReviewEntity;
import vn.chiendt.haimuoi3.review.repository.ProductReviewRepository;
import vn.chiendt.haimuoi3.review.validator.ProductReviewBusinessValidator;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductReviewServiceImplTest {

    @Mock
    private ProductReviewRepository productReviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductReviewBusinessValidator productReviewBusinessValidator;

    @InjectMocks
    private ProductReviewServiceImpl productReviewService;

    @Test
    void getStats_empty_returnsZeros() {
        when(productReviewRepository.findByProductId("p1")).thenReturn(List.of());

        ProductReviewStatsResponse stats = productReviewService.getStats("p1");

        assertThat(stats.getTotalReviews()).isZero();
        assertThat(stats.getAverageRating()).isZero();
        assertThat(stats.getDistribution().get(5)).isZero();
    }

    @Test
    void getStats_computesAverageAndDistribution() {
        when(productReviewRepository.findByProductId("p1")).thenReturn(List.of(
                review(5),
                review(4),
                review(5)
        ));

        ProductReviewStatsResponse stats = productReviewService.getStats("p1");

        assertThat(stats.getTotalReviews()).isEqualTo(3);
        assertThat(stats.getAverageRating()).isEqualTo(4.7);
        assertThat(stats.getDistribution().get(5)).isEqualTo(2L);
        assertThat(stats.getDistribution().get(4)).isEqualTo(1L);
    }

    @Test
    void create_persistsReviewForUserProduct() {
        CreateProductReviewRequest request = new CreateProductReviewRequest();
        request.setRating(5);
        request.setComment("great");
        UserEntity user = UserEntity.builder()
                .id(10L)
                .fullName("Jane")
                .avatarUrl("avatar.png")
                .build();

        when(productRepository.existsById("p1")).thenReturn(true);
        when(productReviewRepository.existsByProductIdAndUserId("p1", 10L)).thenReturn(false);
        when(productReviewRepository.save(any(ProductReviewEntity.class))).thenAnswer(inv -> {
            ProductReviewEntity entity = inv.getArgument(0);
            entity.setId("r1");
            return entity;
        });

        ProductReviewResponse response = productReviewService.create("p1", request, user);

        assertThat(response.getId()).isEqualTo("r1");
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("great");
        assertThat(response.getUserName()).isEqualTo("Jane");
    }

    private static ProductReviewEntity review(int rating) {
        return ProductReviewEntity.builder()
                .id("x")
                .productId("p1")
                .rating(rating)
                .comment("ok")
                .authorDisplayName("u")
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build();
    }
}
