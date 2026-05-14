package vn.chiendt.haimuoi3.sysadmin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateShopRequest {

    private String shopName;
    private String description;
    private String email;
    private String phone;
    private String province;
    private String district;
    private String addressDetail;
}
