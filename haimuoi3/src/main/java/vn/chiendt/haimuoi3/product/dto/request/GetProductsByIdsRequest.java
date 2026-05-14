package vn.chiendt.haimuoi3.product.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetProductsByIdsRequest {
    private List<String> ids;
}
