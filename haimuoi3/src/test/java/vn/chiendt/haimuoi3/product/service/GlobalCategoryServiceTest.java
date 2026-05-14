package vn.chiendt.haimuoi3.product.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.product.dto.request.CreateGlobalCategoryRequest;
import vn.chiendt.haimuoi3.product.dto.request.UpdateGlobalCategoryRequest;
import vn.chiendt.haimuoi3.product.dto.response.GlobalCategoryAdminResponse;
import vn.chiendt.haimuoi3.product.dto.response.GlobalCategoryResponse;
import vn.chiendt.haimuoi3.product.dto.response.GlobalProductResponse;
import vn.chiendt.haimuoi3.product.mapper.GlobalCategoryMapper;
import vn.chiendt.haimuoi3.product.model.postgres.GlobalCategoryEntity;
import vn.chiendt.haimuoi3.product.repository.GlobalCategoryRepository;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.product.service.ProductService;
import vn.chiendt.haimuoi3.product.validator.GlobalCategoryBusinessValidator;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalCategoryServiceTest {

    @Mock
    private GlobalCategoryRepository globalCategoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @Mock
    private GlobalCategoryMapper globalCategoryMapper;

    @Mock
    private GlobalCategoryBusinessValidator globalCategoryBusinessValidator;

    @InjectMocks
    private GlobalCategoryServiceImpl globalCategoryService;

    private GlobalCategoryEntity categoryEntity;
    private GlobalCategoryResponse categoryResponse;
    private GlobalCategoryAdminResponse adminResponse;

    @BeforeEach
    void setUp() {
        categoryEntity = GlobalCategoryEntity.builder()
                .globalCategoryId("cat-123")
                .name("Test Category")
                .slug("test-category")
                .displayOrder(1)
                .isActive(true)
                .build();

        categoryResponse = GlobalCategoryResponse.builder()
                .globalCategoryId("cat-123")
                .name("Test Category")
                .slug("test-category")
                .displayOrder(1)
                .isActive(true)
                .build();

        adminResponse = GlobalCategoryAdminResponse.builder()
                .globalCategoryId("cat-123")
                .name("Test Category")
                .slug("test-category")
                .displayOrder(1)
                .isActive(true)
                .productCount(0L)
                .build();
    }

    @Test
    void findAllPublic_returnsActiveCategories() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<GlobalCategoryEntity> entityPage = new PageImpl<>(List.of(categoryEntity));
        when(globalCategoryRepository.findByIsActiveTrue(pageable)).thenReturn(entityPage);
        when(globalCategoryMapper.toResponse(categoryEntity)).thenReturn(categoryResponse);

        Page<GlobalCategoryResponse> result = globalCategoryService.findAllPublic(pageable);

        assertThat(result.getContent()).containsExactly(categoryResponse);
    }

    @Test
    void findOnePublic_whenInactive_throwsNotFound() {
        GlobalCategoryEntity inactive = GlobalCategoryEntity.builder()
                .globalCategoryId("cat-123")
                .isActive(false)
                .build();
        when(globalCategoryRepository.findById("cat-123")).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> globalCategoryService.findOnePublic("cat-123"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenSlugExists_throwsIllegalArgumentException() {
        CreateGlobalCategoryRequest request = CreateGlobalCategoryRequest.builder()
                .name("New Category")
                .slug("new-category")
                .displayOrder(1)
                .build();
        when(globalCategoryRepository.existsBySlug("new-category")).thenReturn(true);

        assertThatThrownBy(() -> globalCategoryService.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void delete_whenProductsExist_throwsIllegalStateException() {
        when(globalCategoryRepository.findById("cat-123")).thenReturn(Optional.of(categoryEntity));
        when(productRepository.countByGlobalCategoryId("cat-123")).thenReturn(2L);

        assertThatThrownBy(() -> globalCategoryService.delete("cat-123"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void findProductsByCategory_delegatesToProductService() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<GlobalProductResponse> productPage = Page.empty(pageable);
        when(globalCategoryRepository.findById("cat-123")).thenReturn(Optional.of(categoryEntity));
        when(productService.findAllGlobalProduct(null, "cat-123", null, null, null, pageable))
                .thenReturn(productPage);

        Page<GlobalProductResponse> result = globalCategoryService.findProductsByCategory("cat-123", pageable);

        assertThat(result).isSameAs(productPage);
        verify(productService).findAllGlobalProduct(null, "cat-123", null, null, null, pageable);
    }

    @Test
    void update_persistsChanges() {
        UpdateGlobalCategoryRequest request = UpdateGlobalCategoryRequest.builder()
                .name("Updated")
                .build();
        when(globalCategoryRepository.findById("cat-123")).thenReturn(Optional.of(categoryEntity));
        when(globalCategoryRepository.save(categoryEntity)).thenReturn(categoryEntity);
        when(productRepository.countByGlobalCategoryId("cat-123")).thenReturn(0L);
        when(globalCategoryMapper.toAdminResponse(categoryEntity, 0L)).thenReturn(adminResponse);

        GlobalCategoryAdminResponse result = globalCategoryService.update("cat-123", request);

        assertThat(result).isEqualTo(adminResponse);
        verify(globalCategoryBusinessValidator).validateUpdate(request);
    }

    @Test
    void toggleStatus_flipsActiveFlag() {
        when(globalCategoryRepository.findById("cat-123")).thenReturn(Optional.of(categoryEntity));
        when(globalCategoryRepository.save(categoryEntity)).thenReturn(categoryEntity);
        when(productRepository.countByGlobalCategoryId("cat-123")).thenReturn(0L);
        when(globalCategoryMapper.toAdminResponse(categoryEntity, 0L)).thenReturn(adminResponse);

        globalCategoryService.toggleStatus("cat-123");

        assertThat(categoryEntity.isActive()).isFalse();
    }
}
