package vn.chiendt.haimuoi3.product.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import vn.chiendt.haimuoi3.product.dto.request.CreateGlobalCategoryRequest;
import vn.chiendt.haimuoi3.product.dto.request.UpdateGlobalCategoryRequest;
import vn.chiendt.haimuoi3.product.dto.response.GlobalCategoryAdminResponse;
import vn.chiendt.haimuoi3.product.dto.response.GlobalCategoryResponse;
import vn.chiendt.haimuoi3.product.model.postgres.GlobalCategoryEntity;

import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface GlobalCategoryMapper {

    @Mapping(target = "globalCategoryId", ignore = true)
    @Mapping(target = "metaData", expression = "java(buildMetaDataFromCreateRequest(request))")
    GlobalCategoryEntity toEntity(CreateGlobalCategoryRequest request);

    @Mapping(target = "subtitle", expression = "java(extractFromMetaData(entity.getMetaData(), \"subtitle\"))")
    @Mapping(target = "ctaText", expression = "java(extractFromMetaData(entity.getMetaData(), \"ctaText\"))")
    @Mapping(target = "route", expression = "java(extractFromMetaData(entity.getMetaData(), \"route\"))")
    @Mapping(target = "description", expression = "java(extractFromMetaData(entity.getMetaData(), \"description\"))")
    GlobalCategoryResponse toResponse(GlobalCategoryEntity entity);

    @Mapping(target = "subtitle", expression = "java(extractFromMetaData(entity.getMetaData(), \"subtitle\"))")
    @Mapping(target = "ctaText", expression = "java(extractFromMetaData(entity.getMetaData(), \"ctaText\"))")
    @Mapping(target = "route", expression = "java(extractFromMetaData(entity.getMetaData(), \"route\"))")
    @Mapping(target = "description", expression = "java(extractFromMetaData(entity.getMetaData(), \"description\"))")
    @Mapping(target = "productCount", source = "productCount")
    GlobalCategoryAdminResponse toAdminResponse(GlobalCategoryEntity entity, long productCount);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "globalCategoryId", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "metaData", expression = "java(mergeUpdateMetaData(request, entity))")
    void updateEntity(UpdateGlobalCategoryRequest request, @MappingTarget GlobalCategoryEntity entity);

    default Map<String, Object> buildMetaDataFromCreateRequest(CreateGlobalCategoryRequest request) {
        Map<String, Object> metaData = request.getMetaData() != null
                ? new HashMap<>(request.getMetaData())
                : new HashMap<>();

        putIfPresent(metaData, "description", request.getDescription());
        putIfPresent(metaData, "subtitle", request.getSubtitle());
        putIfPresent(metaData, "ctaText", request.getCtaText());
        if (request.getRoute() != null) {
            metaData.put("route", request.getRoute());
        } else if (request.getSlug() != null) {
            metaData.put("route", "/category/" + request.getSlug());
        }
        return metaData;
    }

    default Map<String, Object> mergeUpdateMetaData(UpdateGlobalCategoryRequest request, GlobalCategoryEntity entity) {
        Map<String, Object> metaData = entity.getMetaData() != null
                ? new HashMap<>(entity.getMetaData())
                : new HashMap<>();
        putIfPresent(metaData, "description", request.getDescription());
        putIfPresent(metaData, "subtitle", request.getSubtitle());
        putIfPresent(metaData, "ctaText", request.getCtaText());
        putIfPresent(metaData, "route", request.getRoute());
        return metaData;
    }

    default void putIfPresent(Map<String, Object> metaData, String key, String value) {
        if (value != null) {
            metaData.put(key, value);
        }
    }

    default String extractFromMetaData(Map<String, Object> metaData, String key) {
        if (metaData == null) {
            return null;
        }
        Object value = metaData.get(key);
        return value != null ? value.toString() : null;
    }
}
