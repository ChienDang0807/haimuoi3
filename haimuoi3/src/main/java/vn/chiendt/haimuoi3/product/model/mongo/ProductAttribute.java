package vn.chiendt.haimuoi3.product.model.mongo;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAttribute {
    @Field("name")
    private String name;

    @Field("values")
    private List<String> values;
}
