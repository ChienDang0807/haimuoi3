package vn.chiendt.haimuoi3.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import vn.chiendt.haimuoi3.product.model.ProductKind;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSuggestionResponse {
    private String id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private String status;
    private ProductKind productKind;
}
