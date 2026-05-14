package vn.chiendt.haimuoi3.product.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import vn.chiendt.haimuoi3.inventory.service.InventoryService;
import vn.chiendt.haimuoi3.product.model.ProductBadgeType;
import vn.chiendt.haimuoi3.product.model.ProductKind;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;
import vn.chiendt.haimuoi3.product.model.mongo.ProductPicture;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("dev")
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class ProductDataSeeder implements CommandLineRunner {

    private static final int DEFAULT_SEED_SIZE = 40;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    @Override
    public void run(String... args) {
        try {
            long existingCount = productRepository.count();
            if (existingCount > 0) {
                log.info("Skip product seeding because products already exist: {}", existingCount);
                return;
            }

            List<ProductEntity> products = new ArrayList<>();
            for (int i = 1; i <= DEFAULT_SEED_SIZE; i++) {
                products.add(buildProduct(i));
            }

            productRepository.saveAll(products);

            long shopPk = 1L;
            for (ProductEntity p : products) {
                inventoryService.ensureProductStockRow(shopPk, p.getId());
            }

            ProductEntity demoParent = productRepository.save(buildDemoParent());
            ProductEntity skuA = productRepository.save(buildDemoSku(demoParent.getId(), "DEMO-SKU-A", BigDecimal.valueOf(59), Map.of("Size", "S")));
            ProductEntity skuB = productRepository.save(buildDemoSku(demoParent.getId(), "DEMO-SKU-B", BigDecimal.valueOf(69), Map.of("Size", "M")));
            inventoryService.ensureProductStockRow(shopPk, skuA.getId());
            inventoryService.ensureProductStockRow(shopPk, skuB.getId());

            log.info("Seeded {} LEGACY + 1 PARENT + 2 SKU demo products", products.size());
        } catch (DataAccessException ex) {
            log.warn("Skip product seeding because MongoDB is unavailable: {}", ex.getMessage());
        }
    }

    private ProductEntity buildProduct(int index) {
        ProductBadgeType badgeType;
        int mod = index % 4;
        if (mod == 1) {
            badgeType = ProductBadgeType.NEW;
        } else if (mod == 2) {
            badgeType = ProductBadgeType.SALE;
        } else if (mod == 3) {
            badgeType = ProductBadgeType.RARE;
        } else {
            badgeType = ProductBadgeType.NONE;
        }

        return ProductEntity.builder()
                .name("Home Product " + index)
                .description("Sample product " + index + " for Home page new-arrivals flow")
                .price(BigDecimal.valueOf(49 + index))
                .brand("DemoBrand")
                .globalCategoryId("seed-category")
                .globalCategoryName("General")
                .shopCategoryId("seed-shop-category")
                .shopCategoryName("General")
                .reviewCount((double) ((index % 5) + 1))
                .status("ACTIVE")
                .featured(index % 2 == 0)
                .shopId("1")
                .badgeType(badgeType)
                .productPictures(List.of(
                        ProductPicture.builder()
                                .url("https://picsum.photos/seed/home-product-" + index + "/800/800")
                                .mimiType("image/jpeg")
                                .build()
                ))
                .build();
    }

    private ProductEntity buildDemoParent() {
        return ProductEntity.builder()
                .name("Demo Parent Product (variants)")
                .description("Seeded PARENT with two SKU children for variant / cart testing")
                .price(BigDecimal.ZERO)
                .brand("DemoBrand")
                .globalCategoryId("seed-category")
                .globalCategoryName("General")
                .shopCategoryId("seed-shop-category")
                .shopCategoryName("General")
                .reviewCount(4.0)
                .status("ACTIVE")
                .featured(true)
                .shopId("1")
                .productKind(ProductKind.PARENT)
                .badgeType(ProductBadgeType.NEW)
                .productPictures(List.of(
                        ProductPicture.builder()
                                .url("https://picsum.photos/seed/demo-parent/800/800")
                                .mimiType("image/jpeg")
                                .build()
                ))
                .build();
    }

    private ProductEntity buildDemoSku(String parentId, String skuCode, BigDecimal price, Map<String, String> variantOptions) {
        return ProductEntity.builder()
                .name("Demo variant " + skuCode)
                .description("")
                .price(price)
                .brand("DemoBrand")
                .globalCategoryId("seed-category")
                .globalCategoryName("General")
                .shopCategoryId("seed-shop-category")
                .shopCategoryName("General")
                .reviewCount(4.0)
                .status("ACTIVE")
                .featured(false)
                .shopId("1")
                .productKind(ProductKind.SKU)
                .parentProductId(parentId)
                .sku(skuCode)
                .variantOptions(new LinkedHashMap<>(variantOptions))
                .badgeType(ProductBadgeType.NONE)
                .productPictures(List.of(
                        ProductPicture.builder()
                                .url("https://picsum.photos/seed/" + skuCode + "/800/800")
                                .mimiType("image/jpeg")
                                .build()
                ))
                .build();
    }
}
