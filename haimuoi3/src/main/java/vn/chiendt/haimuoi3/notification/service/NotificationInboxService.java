package vn.chiendt.haimuoi3.notification.service;

import vn.chiendt.haimuoi3.notification.dto.NotificationDTO;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

import java.util.List;

public interface NotificationInboxService {

    List<NotificationDTO> listForCurrentUser(UserEntity currentUser, int limit);

    void markRead(UserEntity currentUser, String notificationId);

    void markAllRead(UserEntity currentUser);
}
