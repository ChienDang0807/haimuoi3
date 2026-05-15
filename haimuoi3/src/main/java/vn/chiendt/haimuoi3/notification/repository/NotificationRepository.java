package vn.chiendt.haimuoi3.notification.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.chiendt.haimuoi3.notification.model.RecipientKind;
import vn.chiendt.haimuoi3.notification.model.postgres.NotificationEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findByRecipientKindAndRecipientIdOrderByCreatedAtDesc(
            RecipientKind recipientKind,
            Long recipientId,
            Pageable pageable);

    Optional<NotificationEntity> findByIdAndRecipientKindAndRecipientId(
            UUID id,
            RecipientKind recipientKind,
            Long recipientId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update NotificationEntity n
            set n.read = true
            where n.recipientKind = :recipientKind
              and n.recipientId = :recipientId
              and n.read = false
            """)
    int markAllRead(
            @Param("recipientKind") RecipientKind recipientKind,
            @Param("recipientId") Long recipientId);
}
