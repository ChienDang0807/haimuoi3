package vn.chiendt.haimuoi3.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.product.dto.request.CreateGlobalCategoryRequest;
import vn.chiendt.haimuoi3.product.dto.response.GlobalCategoryResponse;
import vn.chiendt.haimuoi3.product.service.GlobalCategoryService;

@RestController
@RequestMapping("/api/v1/global-categories")
@RequiredArgsConstructor
public class GlobalCategoryController {

    private final GlobalCategoryService globalCategoryService;

    @GetMapping
    public ApiResponse<Page<GlobalCategoryResponse>> getAllCategories(
            @PageableDefault(size = 20, sort = "displayOrder") Pageable pageable
    ) {
        Page<GlobalCategoryResponse> categories = globalCategoryService.findAllPublic(pageable);
        return ApiResponse.success(categories, "Fetched all global categories");
    }

    @GetMapping("/{id}")
    public ApiResponse<GlobalCategoryResponse> getCategoryById(@PathVariable String id) {
        GlobalCategoryResponse category = globalCategoryService.findOnePublic(id);
        return ApiResponse.success(category, "Fetched global category");
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<GlobalCategoryResponse> createCategory(
            @RequestBody CreateGlobalCategoryRequest request
    ) {
        GlobalCategoryResponse created = globalCategoryService.save(request);
        return ApiResponse.success(created, "Global category created");
    }

    @PatchMapping("/{id}/image-url")
    public ApiResponse<GlobalCategoryResponse> updateImageUrl(
            @PathVariable String id,
            @RequestParam String imageUrl
    ) {
        GlobalCategoryResponse updated = globalCategoryService.updateImageUrl(id, imageUrl)
                .orElseThrow(() -> new ResourceNotFoundException("Global category not found with id: " + id));
        return ApiResponse.success(updated, "Image URL updated");
    }
}
