package vn.chiendt.haimuoi3.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemResponse {
    private String productId;
    private String displayName;
    private String sku;
    private Integer quantityOnHand;
    private boolean lowStock;
}
