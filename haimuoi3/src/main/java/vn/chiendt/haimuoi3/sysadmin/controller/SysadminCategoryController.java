package vn.chiendt.haimuoi3.sysadmin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.product.dto.request.CreateGlobalCategoryRequest;
import vn.chiendt.haimuoi3.product.dto.request.UpdateGlobalCategoryRequest;
import vn.chiendt.haimuoi3.product.dto.response.GlobalCategoryAdminResponse;
import vn.chiendt.haimuoi3.product.dto.response.GlobalProductResponse;
import vn.chiendt.haimuoi3.product.service.GlobalCategoryService;

@RestController
@RequestMapping("/api/v1/sysadmin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SysadminCategoryController {

    private final GlobalCategoryService globalCategoryService;

    @GetMapping
    public ApiResponse<Page<GlobalCategoryAdminResponse>> getAllCategories(
            @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<GlobalCategoryAdminResponse> page = globalCategoryService.findAllForAdmin(pageable);
        return ApiResponse.success(page, "Categories retrieved successfully");
    }

    @GetMapping("/{categoryId}")
    public ApiResponse<GlobalCategoryAdminResponse> getCategoryById(@PathVariable String categoryId) {
        GlobalCategoryAdminResponse response = globalCategoryService.getForAdmin(categoryId);
        return ApiResponse.success(response, "Category retrieved successfully");
    }

    @PostMapping
    public ApiResponse<GlobalCategoryAdminResponse> createCategory(@RequestBody CreateGlobalCategoryRequest request) {
        GlobalCategoryAdminResponse response = globalCategoryService.create(request);
        return ApiResponse.success(response, "Category created successfully");
    }

    @PutMapping("/{categoryId}")
    public ApiResponse<GlobalCategoryAdminResponse> updateCategory(
            @PathVariable String categoryId,
            @RequestBody UpdateGlobalCategoryRequest request) {
        GlobalCategoryAdminResponse response = globalCategoryService.update(categoryId, request);
        return ApiResponse.success(response, "Category updated successfully");
    }

    @PutMapping("/{categoryId}/status")
    public ApiResponse<GlobalCategoryAdminResponse> toggleCategoryStatus(@PathVariable String categoryId) {
        GlobalCategoryAdminResponse response = globalCategoryService.toggleStatus(categoryId);
        return ApiResponse.success(response, "Category status updated successfully");
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable String categoryId) {
        globalCategoryService.delete(categoryId);
        return ApiResponse.success(null, "Category deleted successfully");
    }

    @GetMapping("/{categoryId}/products")
    public ApiResponse<Page<GlobalProductResponse>> getProductsByCategory(
            @PathVariable String categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GlobalProductResponse> page = globalCategoryService.findProductsByCategory(categoryId, pageable);
        return ApiResponse.success(page, "Products retrieved successfully");
    }
}
