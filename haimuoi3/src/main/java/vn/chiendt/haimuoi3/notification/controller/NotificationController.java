package vn.chiendt.haimuoi3.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.notification.dto.NotificationDTO;
import vn.chiendt.haimuoi3.notification.service.NotificationInboxService;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationInboxService notificationInboxService;

    @GetMapping("/me")
    public ApiResponse<List<NotificationDTO>> listMine(
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestParam(name = "limit", defaultValue = "50") int limit) {
        List<NotificationDTO> notifications = notificationInboxService.listForCurrentUser(currentUser, limit);
        return ApiResponse.success(notifications, "Notifications retrieved successfully");
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markRead(
            @AuthenticationPrincipal UserEntity currentUser,
            @PathVariable String id) {
        notificationInboxService.markRead(currentUser, id);
        return ApiResponse.success(null, "Notification marked as read");
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllRead(@AuthenticationPrincipal UserEntity currentUser) {
        notificationInboxService.markAllRead(currentUser);
        return ApiResponse.success(null, "Notifications marked as read");
    }
}
