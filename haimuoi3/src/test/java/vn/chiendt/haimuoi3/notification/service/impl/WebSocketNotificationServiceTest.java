package vn.chiendt.haimuoi3.notification.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import vn.chiendt.haimuoi3.notification.dto.NotificationDTO;
import vn.chiendt.haimuoi3.notification.model.NotificationType;
import vn.chiendt.haimuoi3.notification.model.RecipientKind;
import vn.chiendt.haimuoi3.notification.model.postgres.NotificationEntity;
import vn.chiendt.haimuoi3.notification.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketNotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private WebSocketNotificationService service;

    @Test
    void sendToShop_persistsThenPublishes() {
        String id = UUID.randomUUID().toString();
        NotificationDTO dto = NotificationDTO.builder()
                .id(id)
                .type(NotificationType.ORDER_CREATED)
                .recipientRole("SHOP_OWNER")
                .payload(Map.of("orderId", 1L))
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        when(notificationRepository.save(any(NotificationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.sendToShop(12L, dto);

        ArgumentCaptor<NotificationEntity> entityCaptor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(entityCaptor.capture());
        NotificationEntity saved = entityCaptor.getValue();
        assertThat(saved.getId()).hasToString(id);
        assertThat(saved.getRecipientKind()).isEqualTo(RecipientKind.SHOP);
        assertThat(saved.getRecipientId()).isEqualTo(12L);

        verify(messagingTemplate).convertAndSend(eq("/topic/shop/12/notifications"), eq(dto));
    }
}
