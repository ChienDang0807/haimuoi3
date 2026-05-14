package vn.chiendt.haimuoi3.review.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "product_reviews")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewEntity {

    @Id
    private String id;

    @Indexed
    @Field("product_id")
    private String productId;

    @Indexed
    @Field("user_id")
    private Long userId;

    @Field("rating")
    private int rating;

    @Field("comment")
    private String comment;

    @Field("author_display_name")
    private String authorDisplayName;

    @Field("author_avatar_url")
    private String authorAvatarUrl;

    @Field("created_at")
    private Instant createdAt;
}
