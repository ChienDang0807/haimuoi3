package vn.chiendt.haimuoi3.notification.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.notification.dto.NotificationDTO;
import vn.chiendt.haimuoi3.notification.mapper.NotificationMapper;
import vn.chiendt.haimuoi3.notification.model.RecipientKind;
import vn.chiendt.haimuoi3.notification.model.postgres.NotificationEntity;
import vn.chiendt.haimuoi3.notification.repository.NotificationRepository;
import vn.chiendt.haimuoi3.notification.service.NotificationInboxService;
import vn.chiendt.haimuoi3.notification.validator.NotificationQueryValidator;
import vn.chiendt.haimuoi3.shop.dto.response.ShopResponse;
import vn.chiendt.haimuoi3.shop.service.ShopService;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;
import vn.chiendt.haimuoi3.user.model.postgres.UserRole;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationInboxServiceImpl implements NotificationInboxService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationQueryValidator notificationQueryValidator;
    private final ShopService shopService;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> listForCurrentUser(UserEntity currentUser, int limit) {
        notificationQueryValidator.validateLimit(limit);
        RecipientScope recipientScope = resolveRecipientScope(currentUser);
        return notificationRepository
                .findByRecipientKindAndRecipientIdOrderByCreatedAtDesc(
                        recipientScope.recipientKind(),
                        recipientScope.recipientId(),
                        PageRequest.of(0, limit))
                .stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void markRead(UserEntity currentUser, String notificationId) {
        RecipientScope recipientScope = resolveRecipientScope(currentUser);
        UUID id = UUID.fromString(notificationId);
        NotificationEntity entity = notificationRepository
                .findByIdAndRecipientKindAndRecipientId(
                        id, recipientScope.recipientKind(), recipientScope.recipientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId));
        if (!entity.isRead()) {
            entity.setRead(true);
            notificationRepository.save(entity);
            log.info("Marked notification {} as read for {} recipient {}", id, recipientScope.recipientKind(),
                    recipientScope.recipientId());
        }
    }

    @Override
    @Transactional
    public void markAllRead(UserEntity currentUser) {
        RecipientScope recipientScope = resolveRecipientScope(currentUser);
        int updated = notificationRepository.markAllRead(
                recipientScope.recipientKind(), recipientScope.recipientId());
        log.info("Marked {} notifications as read for {} recipient {}", updated, recipientScope.recipientKind(),
                recipientScope.recipientId());
    }

    private RecipientScope resolveRecipientScope(UserEntity currentUser) {
        if (currentUser.getRole() == UserRole.CUSTOMER) {
            return new RecipientScope(RecipientKind.USER, currentUser.getId());
        }
        if (currentUser.getRole() == UserRole.SHOP_OWNER) {
            ShopResponse shop = shopService.getShopByOwnerId(currentUser.getId());
            return new RecipientScope(RecipientKind.SHOP, shop.getId());
        }
        throw new IllegalArgumentException("Unsupported role for notifications: " + currentUser.getRole());
    }

    private record RecipientScope(RecipientKind recipientKind, Long recipientId) {
    }
}
