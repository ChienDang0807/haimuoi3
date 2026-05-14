package vn.chiendt.haimuoi3.product.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Sort;
import org.bson.Document;
import vn.chiendt.haimuoi3.product.dto.response.ProductPriceRange;
import vn.chiendt.haimuoi3.product.model.ProductKind;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private static final String FIELD_PRODUCT_KIND = "product_kind";

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<ProductEntity> findGlobalProducts(
            String searchQuery,
            String globalCategoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            Pageable pageable
    ) {
        Query query = new Query();
        List<Criteria> criteria = new ArrayList<>();

        criteria.add(Criteria.where("status").regex("^ACTIVE$", "i"));
        criteria.add(catalogListingKindCriteria());
        addSearchCriteria(criteria, searchQuery);

        if (globalCategoryId != null && !globalCategoryId.isBlank()) {
            criteria.add(Criteria.where("global_category_id").is(globalCategoryId));
        }

        if (minPrice != null || maxPrice != null) {
            Criteria priceCriteria = Criteria.where("price");
            if (minPrice != null) {
                priceCriteria = priceCriteria.gte(minPrice);
            }
            if (maxPrice != null) {
                priceCriteria = priceCriteria.lte(maxPrice);
            }
            criteria.add(priceCriteria);
        }

        if (minRating != null) {
            // Du lieu rating co the duoc luu theo mot trong hai key, tuy thuoc migration.
            criteria.add(new Criteria().orOperator(
                    Criteria.where("averageRating").gte(minRating),
                    Criteria.where("average_rating").gte(minRating)
            ));
        }

        query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));

        long total = mongoTemplate.count(query, ProductEntity.class);
        query.with(pageable);
        List<ProductEntity> content = mongoTemplate.find(query, ProductEntity.class);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<ProductEntity> suggestProducts(String searchQuery, int limit) {
        if (searchQuery == null || searchQuery.isBlank() || limit <= 0) {
            return List.of();
        }

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("status").regex("^ACTIVE$", "i"),
                catalogListingKindCriteria(),
                buildPrefixSearchCriteria(searchQuery)
        ));
        query.limit(limit);

        return mongoTemplate.find(query, ProductEntity.class);
    }

    @Override
    public Page<ProductEntity> findProductsForModeration(String searchQuery, String status, Pageable pageable) {
        Query query = new Query();
        List<Criteria> criteria = new ArrayList<>();

        addSearchCriteria(criteria, searchQuery);
        if (status != null && !status.isBlank()) {
            criteria.add(Criteria.where("status").regex("^" + Pattern.quote(status.trim()) + "$", "i"));
        }

        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, ProductEntity.class);
        query.with(pageable);
        List<ProductEntity> content = mongoTemplate.find(query, ProductEntity.class);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ProductEntity> findCatalogByShopId(String shopId, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("shopId").is(shopId),
                catalogListingKindCriteria()
        ));
        long total = mongoTemplate.count(query, ProductEntity.class);
        query.with(pageable);
        List<ProductEntity> content = mongoTemplate.find(query, ProductEntity.class);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<ProductEntity> findActiveSkusByParentIdAndShopId(String parentId, String shopId) {
        Query query = new Query(new Criteria().andOperator(
                Criteria.where("parent_product_id").is(parentId),
                Criteria.where("shopId").is(shopId),
                Criteria.where("status").regex("^ACTIVE$", "i"),
                Criteria.where(FIELD_PRODUCT_KIND).is(ProductKind.SKU.name())
        ));
        query.with(Sort.by(Sort.Direction.ASC, "sku"));
        return mongoTemplate.find(query, ProductEntity.class);
    }

    @Override
    public Map<String, ProductPriceRange> findMinMaxActiveSkuPriceByParentIds(Collection<String> parentProductIds) {
        if (parentProductIds == null || parentProductIds.isEmpty()) {
            return Map.of();
        }
        MatchOperation match = Aggregation.match(
                Criteria.where("parent_product_id").in(parentProductIds)
                        .and(FIELD_PRODUCT_KIND).is(ProductKind.SKU.name())
                        .and("status").regex("^ACTIVE$", "i")
        );
        GroupOperation group = Aggregation.group("parent_product_id")
                .min("price").as("minPrice")
                .max("price").as("maxPrice");
        Aggregation agg = Aggregation.newAggregation(match, group);
        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "products", Document.class);
        Map<String, ProductPriceRange> out = new HashMap<>();
        for (Document doc : results.getMappedResults()) {
            String id = doc.getString("_id");
            BigDecimal min = toBigDecimal(doc.get("minPrice"));
            BigDecimal max = toBigDecimal(doc.get("maxPrice"));
            if (id != null && min != null && max != null) {
                out.put(id, new ProductPriceRange(min, max));
            }
        }
        return out;
    }

    private static Criteria catalogListingKindCriteria() {
        return Criteria.where(FIELD_PRODUCT_KIND).ne(ProductKind.SKU.name());
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return new BigDecimal(value.toString());
    }

    private void addSearchCriteria(List<Criteria> criteria, String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return;
        }
        criteria.add(buildContainsSearchCriteria(searchQuery));
    }

    private Criteria buildPrefixSearchCriteria(String searchQuery) {
        String escaped = Pattern.quote(searchQuery.trim());
        return new Criteria().orOperator(
                Criteria.where("name").regex("^" + escaped, "i"),
                Criteria.where("brand").regex("^" + escaped, "i")
        );
    }

    private Criteria buildContainsSearchCriteria(String searchQuery) {
        String escaped = Pattern.quote(searchQuery.trim());
        return new Criteria().orOperator(
                Criteria.where("name").regex(escaped, "i"),
                Criteria.where("description").regex(escaped, "i"),
                Criteria.where("brand").regex(escaped, "i")
        );
    }

    @Override
    public long countByGlobalCategoryId(String globalCategoryId) {
        if (globalCategoryId == null || globalCategoryId.isBlank()) {
            return 0L;
        }
        Query query = new Query(Criteria.where("global_category_id").is(globalCategoryId));
        return mongoTemplate.count(query, ProductEntity.class);
    }
}
