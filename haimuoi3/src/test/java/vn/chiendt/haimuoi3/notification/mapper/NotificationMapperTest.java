package vn.chiendt.haimuoi3.notification.mapper;

import org.junit.jupiter.api.Test;
import vn.chiendt.haimuoi3.notification.dto.NotificationDTO;
import vn.chiendt.haimuoi3.notification.model.NotificationType;
import vn.chiendt.haimuoi3.notification.model.RecipientKind;
import vn.chiendt.haimuoi3.notification.model.postgres.NotificationEntity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationMapperTest {

    private final NotificationMapper mapper = new NotificationMapperImpl();

    @Test
    void toDto_mapsEntityFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 14, 10, 0);
        NotificationEntity entity = NotificationEntity.builder()
                .id(id)
                .notificationType(NotificationType.ORDER_CREATED)
                .recipientKind(RecipientKind.SHOP)
                .recipientId(7L)
                .recipientRole("SHOP_OWNER")
                .payload(Map.of("orderId", 99))
                .read(false)
                .createdAt(createdAt)
                .build();

        NotificationDTO dto = mapper.toDto(entity);

        assertThat(dto.getId()).isEqualTo(id.toString());
        assertThat(dto.getType()).isEqualTo(NotificationType.ORDER_CREATED);
        assertThat(dto.getRecipientId()).isEqualTo(7L);
        assertThat(dto.getRecipientRole()).isEqualTo("SHOP_OWNER");
        assertThat(dto.getPayload()).isEqualTo(Map.of("orderId", 99));
        assertThat(dto.getRead()).isFalse();
        assertThat(dto.getTimestamp()).isEqualTo(createdAt);
    }
}
