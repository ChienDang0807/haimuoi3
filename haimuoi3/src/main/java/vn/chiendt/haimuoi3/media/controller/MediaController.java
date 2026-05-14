package vn.chiendt.haimuoi3.media.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.media.dto.response.MediaUploadResponse;
import vn.chiendt.haimuoi3.media.model.MediaTargetType;
import vn.chiendt.haimuoi3.media.service.MediaService;
import vn.chiendt.haimuoi3.product.dto.response.GlobalCategoryResponse;
import vn.chiendt.haimuoi3.product.dto.response.ShopProductResponse;
import vn.chiendt.haimuoi3.product.service.GlobalCategoryService;
import vn.chiendt.haimuoi3.product.service.ProductService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final ProductService productService;
    private final GlobalCategoryService globalCategoryService;

    @PostMapping("/upload/{targetType}")
    public ApiResponse<MediaUploadResponse> uploadImage(
            @PathVariable MediaTargetType targetType,
            @RequestPart("file") MultipartFile file
    ) {
        MediaUploadResponse uploaded = mediaService.uploadImage(file, targetType);
        return ApiResponse.<MediaUploadResponse>builder()
                .message("Upload image successfully")
                .result(uploaded)
                .build();
    }

    @PostMapping("/product/{productId}/upload")
    public ApiResponse<ShopProductResponse> uploadProductImage(
            @PathVariable String productId,
            @RequestPart("file") MultipartFile file
    ) {
        ShopProductResponse updatedProduct = productService.uploadProductImage(productId, file);
        return ApiResponse.<ShopProductResponse>builder()
                .message("Upload product image successfully")
                .result(updatedProduct)
                .build();
    }

    @PostMapping("/product/{productId}/upload/multiple")
    public ApiResponse<ShopProductResponse> uploadProductImages(
            @PathVariable String productId,
            @RequestPart("files") MultipartFile[] files
    ) {
        ShopProductResponse updatedProduct = productService.uploadProductImages(productId, files);
        return ApiResponse.<ShopProductResponse>builder()
                .message("Upload product images successfully")
                .result(updatedProduct)
                .build();
    }

    @PostMapping("/global-category/{categoryId}/upload/multiple")
    public ApiResponse<GlobalCategoryResponse> uploadGlobalCategoryImages(
            @PathVariable String categoryId,
            @RequestPart("files") MultipartFile[] files
    ) {
        MultipartFile[] safeFiles = Optional.ofNullable(files).orElse(new MultipartFile[0]);
        List<String> imageUrls = java.util.Arrays.stream(safeFiles)
                .map(file -> mediaService.uploadImage(file, MediaTargetType.GLOBAL_CATEGORY))
                .map(MediaUploadResponse::getUrl)
                .toList();
        GlobalCategoryResponse updatedCategory = globalCategoryService.updateImages(categoryId, imageUrls);

        return ApiResponse.<GlobalCategoryResponse>builder()
                .message("Upload global category images successfully")
                .result(updatedCategory)
                .build();
    }
}
