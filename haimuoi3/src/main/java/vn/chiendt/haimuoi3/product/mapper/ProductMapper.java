package vn.chiendt.haimuoi3.product.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.chiendt.haimuoi3.product.dto.request.CreateProductRequest;
import vn.chiendt.haimuoi3.product.dto.response.CartProductResponse;
import vn.chiendt.haimuoi3.product.dto.response.GlobalProductResponse;
import vn.chiendt.haimuoi3.product.dto.response.ProductSuggestionResponse;
import vn.chiendt.haimuoi3.product.dto.response.ShopProductResponse;
import vn.chiendt.haimuoi3.product.model.mongo.ProductAttribute;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;
import vn.chiendt.haimuoi3.product.model.mongo.ProductPicture;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "globalCategoryId", source = "categoryPublicId")
    @Mapping(target = "globalCategoryName", ignore = true)
    @Mapping(target = "shopCategoryId", ignore = true)
    @Mapping(target = "shopCategoryName", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "featured", source = "featured", defaultValue = "false")
    @Mapping(target = "badgeType", source = "badgeType", defaultValue = "NONE")
    @Mapping(target = "productPictures", source = "pictures")
    ProductEntity toEntity(CreateProductRequest request);

    @Mapping(target = "skus", ignore = true)
    @Mapping(target = "minSkuPrice", ignore = true)
    @Mapping(target = "maxSkuPrice", ignore = true)
    @Mapping(target = "productKind", expression = "java(vn.chiendt.haimuoi3.product.model.ProductKind.resolve(entity))")
    ShopProductResponse toShopResponse(ProductEntity entity);

    @Mapping(target = "imageUrl", expression = "java(extractImageUrl(entity.getProductPictures()))")
    @Mapping(target = "productKind", expression = "java(vn.chiendt.haimuoi3.product.model.ProductKind.resolve(entity))")
    @Mapping(target = "minSkuPrice", ignore = true)
    @Mapping(target = "maxSkuPrice", ignore = true)
    GlobalProductResponse toGlobalResponse(ProductEntity entity);

    @Mapping(target = "imageUrl", expression = "java(extractImageUrl(entity.getProductPictures()))")
    CartProductResponse toCartProductResponse(ProductEntity entity);

    @Mapping(target = "imageUrl", expression = "java(extractImageUrl(entity.getProductPictures()))")
    @Mapping(target = "productKind", expression = "java(vn.chiendt.haimuoi3.product.model.ProductKind.resolve(entity))")
    ProductSuggestionResponse toSuggestionResponse(ProductEntity entity);

    ProductPicture toProductPicture(CreateProductRequest.PictureRequest request);

    ProductAttribute toProductAttribute(CreateProductRequest.AttributeRequest request);

    List<ProductPicture> toProductPictures(List<CreateProductRequest.PictureRequest> requests);

    List<ProductAttribute> toProductAttributes(List<CreateProductRequest.AttributeRequest> requests);

    default String extractImageUrl(List<ProductPicture> productPictures) {
        if (productPictures == null || productPictures.isEmpty()) {
            return null;
        }
        return productPictures.get(0).getUrl();
    }
}
