package vn.chiendt.haimuoi3.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private Long id;
    private String addressName;
    private String recipientName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String streetAddress;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}
