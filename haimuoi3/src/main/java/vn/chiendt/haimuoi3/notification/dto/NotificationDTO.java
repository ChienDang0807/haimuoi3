package vn.chiendt.haimuoi3.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.chiendt.haimuoi3.notification.model.NotificationType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private String id;
    private NotificationType type;
    private Long recipientId;
    private String recipientRole;
    private Object payload;
    private LocalDateTime timestamp;
    private Boolean read;
}
