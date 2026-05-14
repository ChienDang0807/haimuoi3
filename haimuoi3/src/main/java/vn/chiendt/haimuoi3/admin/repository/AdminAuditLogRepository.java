package vn.chiendt.haimuoi3.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.chiendt.haimuoi3.admin.model.AdminAuditLogEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLogEntity, Long> {

    /**
     * Find all audit logs by user ID
     * @param userId the ID of the user who performed the action
     * @return list of audit logs
     */
    List<AdminAuditLogEntity> findByUserId(Long userId);

    /**
     * Find all audit logs by user ID with pagination
     * @param userId the ID of the user who performed the action
     * @param pageable pagination information
     * @return page of audit logs
     */
    Page<AdminAuditLogEntity> findByUserId(Long userId, Pageable pageable);

    /**
     * Find all audit logs by action
     * @param action the action performed (CREATE, UPDATE, DELETE, etc.)
     * @return list of audit logs
     */
    List<AdminAuditLogEntity> findByAction(String action);

    /**
     * Find all audit logs by action with pagination
     * @param action the action performed (CREATE, UPDATE, DELETE, etc.)
     * @param pageable pagination information
     * @return page of audit logs
     */
    Page<AdminAuditLogEntity> findByAction(String action, Pageable pageable);

    /**
     * Find all audit logs by entity type
     * @param entityType the type of entity affected (USER, SHOP, CATEGORY, etc.)
     * @return list of audit logs
     */
    List<AdminAuditLogEntity> findByEntityType(String entityType);

    /**
     * Find all audit logs by entity type with pagination
     * @param entityType the type of entity affected (USER, SHOP, CATEGORY, etc.)
     * @param pageable pagination information
     * @return page of audit logs
     */
    Page<AdminAuditLogEntity> findByEntityType(String entityType, Pageable pageable);

    /**
     * Find all audit logs by entity type and entity ID
     * @param entityType the type of entity affected
     * @param entityId the ID of the affected entity
     * @return list of audit logs
     */
    List<AdminAuditLogEntity> findByEntityTypeAndEntityId(String entityType, String entityId);

    /**
     * Find all audit logs within a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of audit logs
     */
    List<AdminAuditLogEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all audit logs within a date range with pagination
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return page of audit logs
     */
    Page<AdminAuditLogEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
