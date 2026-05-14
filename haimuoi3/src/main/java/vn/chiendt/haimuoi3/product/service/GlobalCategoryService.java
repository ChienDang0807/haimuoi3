package vn.chiendt.haimuoi3.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.chiendt.haimuoi3.product.dto.request.CreateGlobalCategoryRequest;
import vn.chiendt.haimuoi3.product.dto.request.UpdateGlobalCategoryRequest;
import vn.chiendt.haimuoi3.product.dto.response.GlobalCategoryAdminResponse;
import vn.chiendt.haimuoi3.product.dto.response.GlobalCategoryResponse;
import vn.chiendt.haimuoi3.product.dto.response.GlobalProductResponse;

import java.util.List;
import java.util.Optional;

public interface GlobalCategoryService {

    Page<GlobalCategoryResponse> findAllPublic(Pageable pageable);

    GlobalCategoryResponse findOnePublic(String id);

    Page<GlobalCategoryAdminResponse> findAllForAdmin(Pageable pageable);

    GlobalCategoryAdminResponse getForAdmin(String categoryId);

    GlobalCategoryAdminResponse create(CreateGlobalCategoryRequest request);

    GlobalCategoryAdminResponse update(String categoryId, UpdateGlobalCategoryRequest request);

    GlobalCategoryAdminResponse toggleStatus(String categoryId);

    void delete(String categoryId);

    Page<GlobalProductResponse> findProductsByCategory(String categoryId, Pageable pageable);

    GlobalCategoryResponse save(CreateGlobalCategoryRequest categoryToCreate);

    Optional<GlobalCategoryResponse> findOne(String id);

    Optional<GlobalCategoryResponse> updateImageUrl(String id, String imageUrl);

    GlobalCategoryResponse updateImages(String id, List<String> imageUrls);
}
