package vn.chiendt.haimuoi3.inventory.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdjustStockRequest {

    private Integer quantityOnHand;
}
