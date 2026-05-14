package vn.chiendt.haimuoi3.product.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import vn.chiendt.haimuoi3.product.service.GlobalCategoryService;
import vn.chiendt.haimuoi3.product.service.ProductService;
import vn.chiendt.haimuoi3.product.validator.GlobalCategoryBusinessValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlobalCategoryServiceImpl implements GlobalCategoryService {

    private final GlobalCategoryRepository globalCategoryRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final GlobalCategoryMapper globalCategoryMapper;
    private final GlobalCategoryBusinessValidator globalCategoryBusinessValidator;

    @Override
    @Transactional(readOnly = true)
    public Page<GlobalCategoryResponse> findAllPublic(Pageable pageable) {
        return globalCategoryRepository.findByIsActiveTrue(pageable)
                .map(globalCategoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalCategoryResponse findOnePublic(String id) {
        GlobalCategoryEntity category = globalCategoryRepository.findById(id)
                .filter(GlobalCategoryEntity::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Global category not found with id: " + id));
        return globalCategoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GlobalCategoryAdminResponse> findAllForAdmin(Pageable pageable) {
        return globalCategoryRepository.findAll(pageable)
                .map(entity -> toAdminResponse(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalCategoryAdminResponse getForAdmin(String categoryId) {
        globalCategoryBusinessValidator.validateCategoryId(categoryId);
        GlobalCategoryEntity category = requireCategory(categoryId);
        return toAdminResponse(category);
    }

    @Override
    @Transactional
    public GlobalCategoryAdminResponse create(CreateGlobalCategoryRequest request) {
        globalCategoryBusinessValidator.validateCreate(request);
        assertSlugAvailable(request.getSlug());
        GlobalCategoryEntity entity = globalCategoryMapper.toEntity(request);
        ensureRouteExists(entity);
        GlobalCategoryEntity saved = globalCategoryRepository.save(entity);
        log.info("Global category created id={}", saved.getGlobalCategoryId());
        return toAdminResponse(saved);
    }

    @Override
    @Transactional
    public GlobalCategoryAdminResponse update(String categoryId, UpdateGlobalCategoryRequest request) {
        globalCategoryBusinessValidator.validateCategoryId(categoryId);
        globalCategoryBusinessValidator.validateUpdate(request);
        GlobalCategoryEntity category = requireCategory(categoryId);
        globalCategoryMapper.updateEntity(request, category);
        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            category.setName(request.getName());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        GlobalCategoryEntity updated = globalCategoryRepository.save(category);
        log.info("Global category updated id={}", categoryId);
        return toAdminResponse(updated);
    }

    @Override
    @Transactional
    public GlobalCategoryAdminResponse toggleStatus(String categoryId) {
        globalCategoryBusinessValidator.validateCategoryId(categoryId);
        GlobalCategoryEntity category = requireCategory(categoryId);
        category.setActive(!category.isActive());
        GlobalCategoryEntity updated = globalCategoryRepository.save(category);
        log.info("Global category status toggled id={} active={}", categoryId, updated.isActive());
        return toAdminResponse(updated);
    }

    @Override
    @Transactional
    public void delete(String categoryId) {
        globalCategoryBusinessValidator.validateCategoryId(categoryId);
        GlobalCategoryEntity category = requireCategory(categoryId);
        long productCount = productRepository.countByGlobalCategoryId(categoryId);
        if (productCount > 0) {
            throw new IllegalStateException("Cannot delete category with existing products");
        }
        globalCategoryRepository.delete(category);
        log.info("Global category deleted id={}", categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GlobalProductResponse> findProductsByCategory(String categoryId, Pageable pageable) {
        globalCategoryBusinessValidator.validateCategoryId(categoryId);
        requireCategory(categoryId);
        return productService.findAllGlobalProduct(null, categoryId, null, null, null, pageable);
    }

    @Override
    @Transactional
    public GlobalCategoryResponse save(CreateGlobalCategoryRequest categoryToCreate) {
        GlobalCategoryAdminResponse created = create(categoryToCreate);
        return globalCategoryMapper.toResponse(requireCategory(created.getGlobalCategoryId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GlobalCategoryResponse> findOne(String id) {
        return globalCategoryRepository.findById(id).map(globalCategoryMapper::toResponse);
    }

    @Override
    @Transactional
    public Optional<GlobalCategoryResponse> updateImageUrl(String id, String imageUrl) {
        globalCategoryBusinessValidator.validateImageUrl(imageUrl);
        int updatedRows = globalCategoryRepository.updateImageUrl(id, imageUrl);
        if (updatedRows > 0) {
            return findOne(id);
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public GlobalCategoryResponse updateImages(String id, List<String> imageUrls) {
        globalCategoryBusinessValidator.validateImageUrls(imageUrls);
        GlobalCategoryEntity existing = requireCategory(id);
        existing.setImageUrl(imageUrls.get(0));
        Map<String, Object> metaData = existing.getMetaData() == null
                ? new HashMap<>()
                : new HashMap<>(existing.getMetaData());
        metaData.put("images", imageUrls);
        existing.setMetaData(metaData);
        GlobalCategoryEntity updated = globalCategoryRepository.save(existing);
        return globalCategoryMapper.toResponse(updated);
    }

    private GlobalCategoryAdminResponse toAdminResponse(GlobalCategoryEntity entity) {
        long productCount = productRepository.countByGlobalCategoryId(entity.getGlobalCategoryId());
        return globalCategoryMapper.toAdminResponse(entity, productCount);
    }

    private GlobalCategoryEntity requireCategory(String categoryId) {
        return globalCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Global category not found with id: " + categoryId));
    }

    private void assertSlugAvailable(String slug) {
        if (globalCategoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Category with slug '" + slug + "' already exists");
        }
    }

    private void ensureRouteExists(GlobalCategoryEntity entity) {
        if (entity.getMetaData() == null) {
            entity.setMetaData(new HashMap<>());
        }
        Map<String, Object> metaData = entity.getMetaData();
        if (metaData.get("route") == null && entity.getSlug() != null) {
            metaData.put("route", "/category/" + entity.getSlug());
        }
    }
}
