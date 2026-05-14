package vn.chiendt.haimuoi3.product.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.chiendt.haimuoi3.inventory.model.postgres.ProductStockEntity;
import vn.chiendt.haimuoi3.inventory.repository.ProductStockRepository;
import vn.chiendt.haimuoi3.product.model.ProductBuyerAvailability;
import vn.chiendt.haimuoi3.product.model.ProductKind;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductBuyerAvailabilityServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductStockRepository productStockRepository;

    @InjectMocks
    private ProductBuyerAvailabilityServiceImpl service;

    private ProductEntity activeParent;
    private ProductEntity sku1;
    private ProductEntity sku2;

    @BeforeEach
    void setUp() {
        activeParent = ProductEntity.builder()
                .id("parent-1")
                .shopId("10")
                .status("ACTIVE")
                .productKind(ProductKind.PARENT)
                .build();
        sku1 = ProductEntity.builder()
                .id("sku-1")
                .shopId("10")
                .status("ACTIVE")
                .productKind(ProductKind.SKU)
                .parentProductId("parent-1")
                .build();
        sku2 = ProductEntity.builder()
                .id("sku-2")
                .shopId("10")
                .status("ACTIVE")
                .productKind(ProductKind.SKU)
                .parentProductId("parent-1")
                .build();
    }

    @Test
    void resolveAvailability_blankId_returnsDiscontinued() {
        assertThat(service.resolveAvailabilityByProductId("  ")).isEqualTo(ProductBuyerAvailability.DISCONTINUED);
    }

    @Test
    void resolveAvailability_missingProduct_returnsDiscontinued() {
        when(productRepository.findById("x")).thenReturn(Optional.empty());
        assertThat(service.resolveAvailabilityByProductId("x")).isEqualTo(ProductBuyerAvailability.DISCONTINUED);
    }

    @Test
    void resolveAvailability_inactiveStatus_returnsDiscontinued() {
        ProductEntity inactive = ProductEntity.builder()
                .id("p")
                .shopId("1")
                .status("DRAFT")
                .productKind(ProductKind.PARENT)
                .build();
        assertThat(service.resolveAvailability(inactive)).isEqualTo(ProductBuyerAvailability.DISCONTINUED);
    }

    @Test
    void parent_active_noSkus_returnsOutOfStock() {
        when(productRepository.findActiveSkusByParentIdAndShopId("parent-1", "10")).thenReturn(List.of());
        assertThat(service.resolveAvailability(activeParent)).isEqualTo(ProductBuyerAvailability.OUT_OF_STOCK);
    }

    @Test
    void parent_active_skusAllZeroStock_returnsOutOfStock() {
        when(productRepository.findActiveSkusByParentIdAndShopId("parent-1", "10")).thenReturn(List.of(sku1, sku2));
        when(productStockRepository.findByShopIdAndProductId(10L, "sku-1"))
                .thenReturn(Optional.of(stock(0)));
        when(productStockRepository.findByShopIdAndProductId(10L, "sku-2"))
                .thenReturn(Optional.of(stock(0)));
        assertThat(service.resolveAvailability(activeParent)).isEqualTo(ProductBuyerAvailability.OUT_OF_STOCK);
    }

    @Test
    void parent_active_oneSkuHasStock_returnsAvailable() {
        when(productRepository.findActiveSkusByParentIdAndShopId("parent-1", "10")).thenReturn(List.of(sku1, sku2));
        when(productStockRepository.findByShopIdAndProductId(10L, "sku-1"))
                .thenReturn(Optional.of(stock(0)));
        when(productStockRepository.findByShopIdAndProductId(10L, "sku-2"))
                .thenReturn(Optional.of(stock(3)));
        assertThat(service.resolveAvailability(activeParent)).isEqualTo(ProductBuyerAvailability.AVAILABLE);
    }

    @Test
    void sku_active_zeroStock_returnsOutOfStock() {
        ProductEntity sku = ProductEntity.builder()
                .id("sku-x")
                .shopId("5")
                .status("ACTIVE")
                .productKind(ProductKind.SKU)
                .build();
        when(productStockRepository.findByShopIdAndProductId(5L, "sku-x"))
                .thenReturn(Optional.of(stock(0)));
        assertThat(service.resolveAvailability(sku)).isEqualTo(ProductBuyerAvailability.OUT_OF_STOCK);
    }

    @Test
    void sku_active_positiveStock_returnsAvailable() {
        ProductEntity sku = ProductEntity.builder()
                .id("sku-x")
                .shopId("5")
                .status("ACTIVE")
                .productKind(ProductKind.SKU)
                .build();
        when(productStockRepository.findByShopIdAndProductId(5L, "sku-x"))
                .thenReturn(Optional.of(stock(2)));
        assertThat(service.resolveAvailability(sku)).isEqualTo(ProductBuyerAvailability.AVAILABLE);
    }

    private static ProductStockEntity stock(int qty) {
        return ProductStockEntity.builder()
                .quantityOnHand(qty)
                .build();
    }
}
