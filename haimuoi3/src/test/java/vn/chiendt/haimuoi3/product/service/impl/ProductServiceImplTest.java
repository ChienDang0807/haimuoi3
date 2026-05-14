package vn.chiendt.haimuoi3.product.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import vn.chiendt.haimuoi3.inventory.service.InventoryService;
import vn.chiendt.haimuoi3.media.service.MediaService;
import vn.chiendt.haimuoi3.product.dto.response.GlobalProductResponse;
import vn.chiendt.haimuoi3.product.mapper.ProductMapper;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.product.validator.ProductBusinessValidator;
import vn.chiendt.haimuoi3.product.validator.ProductParentSkuValidator;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductBusinessValidator productBusinessValidator;

    @Mock
    private MediaService mediaService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private ProductParentSkuValidator productParentSkuValidator;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void findAllGlobalProduct_passesFiltersToRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        ProductEntity entity = ProductEntity.builder()
                .id("p1")
                .status("ACTIVE")
                .build();
        GlobalProductResponse mapped = GlobalProductResponse.builder().id("p1").build();

        when(productRepository.findGlobalProducts("watch", "cat-1", BigDecimal.TEN, BigDecimal.valueOf(100), 4.0, pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(productMapper.toGlobalResponse(entity)).thenReturn(mapped);

        var result = productService.findAllGlobalProduct(
                "watch",
                "cat-1",
                BigDecimal.TEN,
                BigDecimal.valueOf(100),
                4.0,
                pageable
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(GlobalProductResponse::getId).containsExactly("p1");
        verify(productBusinessValidator).validateGlobalFilter("watch", BigDecimal.TEN, BigDecimal.valueOf(100), 4.0);
        verify(productRepository).findGlobalProducts("watch", "cat-1", BigDecimal.TEN, BigDecimal.valueOf(100), 4.0, pageable);
        verify(productMapper).toGlobalResponse(entity);
    }

    @Test
    void findAllGlobalProduct_withoutFiltersStillWorks() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findGlobalProducts(null, null, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var result = productService.findAllGlobalProduct(null, null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isZero();
        verify(productBusinessValidator).validateGlobalFilter(null, null, null, null);
        verify(productRepository).findGlobalProducts(null, null, null, null, null, pageable);
        verifyNoInteractions(productMapper);
    }
}
