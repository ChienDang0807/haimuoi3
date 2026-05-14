package vn.chiendt.haimuoi3.sysadmin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopListResponse {

    private List<ShopResponse> shops;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}
