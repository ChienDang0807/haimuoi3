package vn.chiendt.haimuoi3.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.chiendt.haimuoi3.admin.dto.response.AdminDashboardMetricsResponse;
import vn.chiendt.haimuoi3.admin.dto.response.AdminModerationProductResponse;
import vn.chiendt.haimuoi3.admin.service.AdminDashboardService;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/metrics")
    public ApiResponse<AdminDashboardMetricsResponse> metrics(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            @RequestParam(required = false, defaultValue = "day") String bucket
    ) {
        AdminDashboardMetricsResponse response = adminDashboardService.getMetrics(from, to, bucket);
        return ApiResponse.success(response, "Fetched admin dashboard metrics");
    }

    @GetMapping("/moderation/products")
    public ApiResponse<Page<AdminModerationProductResponse>> moderationProducts(
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<AdminModerationProductResponse> page = adminDashboardService.searchModerationProducts(
                query,
                status,
                pageable
        );
        return ApiResponse.success(page, "Fetched moderation products");
    }
}
