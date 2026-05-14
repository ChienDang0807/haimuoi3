package vn.chiendt.haimuoi3.product.model.mongo;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPicture {
    @Field("url")
    private String url;
    @Field("mimeType")
    private String mimiType;
}
