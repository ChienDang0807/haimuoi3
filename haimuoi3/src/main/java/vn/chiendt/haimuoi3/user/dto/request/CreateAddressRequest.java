package vn.chiendt.haimuoi3.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAddressRequest {
    private String addressName;
    private String recipientName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String streetAddress;
}
