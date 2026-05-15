package vn.chiendt.haimuoi3.notification.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.chiendt.haimuoi3.notification.dto.NotificationDTO;
import vn.chiendt.haimuoi3.notification.model.RecipientKind;
import vn.chiendt.haimuoi3.notification.model.postgres.NotificationEntity;
import vn.chiendt.haimuoi3.notification.repository.NotificationRepository;
import vn.chiendt.haimuoi3.notification.service.NotificationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    @Override
    public void sendToShop(Long shopId, NotificationDTO notification) {
        persist(RecipientKind.SHOP, shopId, notification);
        String destination = "/topic/shop/" + shopId + "/notifications";
        log.info("Sending notification to shop {}: {}", shopId, notification.getType());
        messagingTemplate.convertAndSend(destination, notification);
    }

    @Override
    public void sendToUser(Long userId, NotificationDTO notification) {
        persist(RecipientKind.USER, userId, notification);
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

    private void persist(RecipientKind recipientKind, Long recipientId, NotificationDTO notification) {
        notification.setRecipientId(recipientId);
        NotificationEntity entity = NotificationEntity.builder()
                .id(UUID.fromString(notification.getId()))
                .notificationType(notification.getType())
                .recipientKind(recipientKind)
                .recipientId(recipientId)
                .recipientRole(resolveRecipientRole(notification))
                .payload(toPayloadMap(notification.getPayload()))
                .read(Boolean.TRUE.equals(notification.getRead()))
                .createdAt(notification.getTimestamp() != null ? notification.getTimestamp() : LocalDateTime.now())
                .build();
        notificationRepository.save(entity);
    }

    private String resolveRecipientRole(NotificationDTO notification) {
        if (notification.getRecipientRole() != null && !notification.getRecipientRole().isBlank()) {
            return notification.getRecipientRole();
        }
        return "UNKNOWN";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toPayloadMap(Object payload) {
        if (payload instanceof Map<?, ?> map) {
            return new HashMap<>((Map<String, Object>) map);
        }
        return Map.of();
    }
}
