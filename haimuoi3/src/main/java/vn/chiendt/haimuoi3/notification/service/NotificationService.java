package vn.chiendt.haimuoi3.notification.service;

import vn.chiendt.haimuoi3.notification.dto.NotificationDTO;

public interface NotificationService {
    
    void sendToUser(Long userId, NotificationDTO notification);
    
    void sendToShop(Long shopId, NotificationDTO notification);
    
    void sendToRole(String role, NotificationDTO notification);
}
