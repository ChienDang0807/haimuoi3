package vn.chiendt.haimuoi3.notification.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.chiendt.haimuoi3.notification.dto.NotificationDTO;
import vn.chiendt.haimuoi3.notification.service.NotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendToShop(Long shopId, NotificationDTO notification) {
        String destination = "/topic/shop/" + shopId + "/notifications";
        log.info("Sending notification to shop {}: {}", shopId, notification.getType());
        messagingTemplate.convertAndSend(destination, notification);
    }

    @Override
    public void sendToUser(Long userId, NotificationDTO notification) {
        String destination = "/queue/user/" + userId + "/notifications";
        log.info("Sending notification to user {}: {}", userId, notification.getType());
        messagingTemplate.convertAndSend(destination, notification);
    }

    @Override
    public void sendToRole(String role, NotificationDTO notification) {
        String destination = "/topic/role/" + role + "/notifications";
        log.info("Sending notification to role {}: {}", role, notification.getType());
        messagingTemplate.convertAndSend(destination, notification);
    }
}
