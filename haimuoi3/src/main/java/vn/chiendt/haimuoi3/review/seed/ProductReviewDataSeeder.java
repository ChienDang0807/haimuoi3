package vn.chiendt.haimuoi3.review.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.product.model.ProductKind;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.review.model.mongo.ProductReviewEntity;
import vn.chiendt.haimuoi3.review.repository.ProductReviewRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("dev")
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class ProductReviewDataSeeder implements CommandLineRunner {

    private final ProductReviewRepository productReviewRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        try {
            if (productReviewRepository.count() > 0) {
                log.info("Skip product review seeding: {} reviews already exist", productReviewRepository.count());
                return;
            }

            List<ProductEntity> products = productRepository.findAll().stream()
                    .filter(p -> ProductKind.resolve(p) != ProductKind.PARENT)
                    .limit(8)
                    .toList();
            if (products.isEmpty()) {
                log.info("Skip product review seeding: no products");
                return;
            }

            List<ProductReviewEntity> batch = new ArrayList<>();
            int seq = 0;
            for (ProductEntity p : products) {
                for (int i = 0; i < 3; i++) {
                    int rating = 4 + (seq % 2);
                    batch.add(ProductReviewEntity.builder()
                            .productId(p.getId())
                            .rating(rating)
                            .comment("Seeded review " + (i + 1) + " for product " + p.getName())
                            .authorDisplayName("Reviewer " + (++seq))
                            .createdAt(Instant.now().minusSeconds(seq * 3600L))
                            .build());
                }
            }

            productReviewRepository.saveAll(batch);
            log.info("Seeded {} product reviews", batch.size());
        } catch (DataAccessException ex) {
            log.warn("Skip product review seeding: {}", ex.getMessage());
        }
    }
}
