package vn.chiendt.haimuoi3.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import vn.chiendt.haimuoi3.review.model.mongo.ProductReviewEntity;

import java.util.List;

public interface ProductReviewRepository extends MongoRepository<ProductReviewEntity, String> {

    Page<ProductReviewEntity> findByProductIdOrderByCreatedAtDesc(String productId, Pageable pageable);

    List<ProductReviewEntity> findByProductId(String productId);

    boolean existsByProductIdAndUserId(String productId, Long userId);
}
