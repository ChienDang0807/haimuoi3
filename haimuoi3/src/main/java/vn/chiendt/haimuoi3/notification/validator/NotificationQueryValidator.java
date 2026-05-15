package vn.chiendt.haimuoi3.notification.validator;

import org.springframework.stereotype.Component;

@Component
public class NotificationQueryValidator {

    public void validateLimit(int limit) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Notification limit must be between 1 and 100");
        }
    }
}
