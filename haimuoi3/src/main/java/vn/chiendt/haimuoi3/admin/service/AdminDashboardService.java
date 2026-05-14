package vn.chiendt.haimuoi3.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.chiendt.haimuoi3.admin.dto.response.AdminDashboardMetricsResponse;
import vn.chiendt.haimuoi3.admin.dto.response.AdminModerationProductResponse;

import java.time.LocalDate;

public interface AdminDashboardService {

    AdminDashboardMetricsResponse getMetrics(LocalDate from, LocalDate to, String bucket);

    Page<AdminModerationProductResponse> searchModerationProducts(String query, String status, Pageable pageable);
}
