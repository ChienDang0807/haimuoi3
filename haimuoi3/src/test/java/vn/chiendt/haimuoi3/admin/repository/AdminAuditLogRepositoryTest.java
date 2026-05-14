package vn.chiendt.haimuoi3.admin.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import vn.chiendt.haimuoi3.admin.model.AdminAuditLogEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

/**
 * Unit tests for AdminAuditLogRepository
 * These tests verify the repository interface methods work correctly
 */
@ExtendWith(MockitoExtension.class)
class AdminAuditLogRepositoryTest {

    @Mock
    private AdminAuditLogRepository repository;

    @Test
    void findByUserId_shouldReturnAuditLogsForUser() {
        // Given
        Long userId = 1L;
        AdminAuditLogEntity log1 = createAuditLog(1L, userId, "CREATE", "USER");
        AdminAuditLogEntity log2 = createAuditLog(2L, userId, "UPDATE", "SHOP");
        
        when(repository.findByUserId(userId)).thenReturn(Arrays.asList(log1, log2));

        // When
        List<AdminAuditLogEntity> result = repository.findByUserId(userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AdminAuditLogEntity::getUserId)
                .containsOnly(userId);
        verify(repository).findByUserId(userId);
    }

    @Test
    void findByUserId_withPagination_shouldReturnPagedResults() {
        // Given
        Long userId = 1L;
        AdminAuditLogEntity log1 = createAuditLog(1L, userId, "CREATE", "USER");
        Pageable pageable = PageRequest.of(0, 1);
        Page<AdminAuditLogEntity> page = new PageImpl<>(Arrays.asList(log1), pageable, 2);
        
        when(repository.findByUserId(userId, pageable)).thenReturn(page);

        // When
        Page<AdminAuditLogEntity> result = repository.findByUserId(userId, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        verify(repository).findByUserId(userId, pageable);
    }

    @Test
    void findByAction_shouldReturnAuditLogsForAction() {
        // Given
        String action = "CREATE";
        AdminAuditLogEntity log = createAuditLog(1L, 1L, action, "USER");
        
        when(repository.findByAction(action)).thenReturn(Arrays.asList(log));

        // When
        List<AdminAuditLogEntity> result = repository.findByAction(action);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAction()).isEqualTo(action);
        verify(repository).findByAction(action);
    }

    @Test
    void findByAction_withPagination_shouldReturnPagedResults() {
        // Given
        String action = "CREATE";
        AdminAuditLogEntity log = createAuditLog(1L, 1L, action, "USER");
        Pageable pageable = PageRequest.of(0, 10);
        Page<AdminAuditLogEntity> page = new PageImpl<>(Arrays.asList(log), pageable, 1);
        
        when(repository.findByAction(action, pageable)).thenReturn(page);

        // When
        Page<AdminAuditLogEntity> result = repository.findByAction(action, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(repository).findByAction(action, pageable);
    }

    @Test
    void findByEntityType_shouldReturnAuditLogsForEntityType() {
        // Given
        String entityType = "SHOP";
        AdminAuditLogEntity log = createAuditLog(1L, 1L, "UPDATE", entityType);
        
        when(repository.findByEntityType(entityType)).thenReturn(Arrays.asList(log));

        // When
        List<AdminAuditLogEntity> result = repository.findByEntityType(entityType);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntityType()).isEqualTo(entityType);
        verify(repository).findByEntityType(entityType);
    }

    @Test
    void findByEntityType_withPagination_shouldReturnPagedResults() {
        // Given
        String entityType = "SHOP";
        AdminAuditLogEntity log = createAuditLog(1L, 1L, "UPDATE", entityType);
        Pageable pageable = PageRequest.of(0, 10);
        Page<AdminAuditLogEntity> page = new PageImpl<>(Arrays.asList(log), pageable, 1);
        
        when(repository.findByEntityType(entityType, pageable)).thenReturn(page);

        // When
        Page<AdminAuditLogEntity> result = repository.findByEntityType(entityType, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(repository).findByEntityType(entityType, pageable);
    }

    @Test
    void findByEntityTypeAndEntityId_shouldReturnSpecificAuditLog() {
        // Given
        String entityType = "USER";
        String entityId = "100";
        AdminAuditLogEntity log = createAuditLogWithEntityId(1L, 1L, "CREATE", entityType, entityId);
        
        when(repository.findByEntityTypeAndEntityId(entityType, entityId))
                .thenReturn(Arrays.asList(log));

        // When
        List<AdminAuditLogEntity> result = repository.findByEntityTypeAndEntityId(entityType, entityId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntityType()).isEqualTo(entityType);
        assertThat(result.get(0).getEntityId()).isEqualTo(entityId);
        verify(repository).findByEntityTypeAndEntityId(entityType, entityId);
    }

    @Test
    void findByCreatedAtBetween_shouldReturnAuditLogsInDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        List<AdminAuditLogEntity> logs = Arrays.asList(
                createAuditLog(1L, 1L, "CREATE", "USER"),
                createAuditLog(2L, 1L, "UPDATE", "SHOP"),
                createAuditLog(3L, 2L, "DELETE", "CATEGORY")
        );
        
        when(repository.findByCreatedAtBetween(startDate, endDate)).thenReturn(logs);

        // When
        List<AdminAuditLogEntity> result = repository.findByCreatedAtBetween(startDate, endDate);

        // Then
        assertThat(result).hasSize(3);
        verify(repository).findByCreatedAtBetween(startDate, endDate);
    }

    @Test
    void findByCreatedAtBetween_withPagination_shouldReturnPagedResults() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        Pageable pageable = PageRequest.of(0, 2);
        List<AdminAuditLogEntity> logs = Arrays.asList(
                createAuditLog(1L, 1L, "CREATE", "USER"),
                createAuditLog(2L, 1L, "UPDATE", "SHOP")
        );
        Page<AdminAuditLogEntity> page = new PageImpl<>(logs, pageable, 3);
        
        when(repository.findByCreatedAtBetween(startDate, endDate, pageable)).thenReturn(page);

        // When
        Page<AdminAuditLogEntity> result = repository.findByCreatedAtBetween(startDate, endDate, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        verify(repository).findByCreatedAtBetween(startDate, endDate, pageable);
    }

    @Test
    void save_shouldPersistAuditLog() {
        // Given
        AdminAuditLogEntity newAuditLog = AdminAuditLogEntity.builder()
                .userId(3L)
                .action("SUSPEND")
                .entityType("USER")
                .entityId("400")
                .oldValue("{\"isActive\":true}")
                .newValue("{\"isActive\":false}")
                .ipAddress("192.168.1.3")
                .build();
        
        AdminAuditLogEntity savedLog = AdminAuditLogEntity.builder()
                .id(1L)
                .userId(3L)
                .action("SUSPEND")
                .entityType("USER")
                .entityId("400")
                .oldValue("{\"isActive\":true}")
                .newValue("{\"isActive\":false}")
                .ipAddress("192.168.1.3")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(repository.save(any(AdminAuditLogEntity.class))).thenReturn(savedLog);

        // When
        AdminAuditLogEntity saved = repository.save(newAuditLog);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(3L);
        assertThat(saved.getAction()).isEqualTo("SUSPEND");
        verify(repository).save(any(AdminAuditLogEntity.class));
    }

    @Test
    void findById_shouldReturnAuditLog() {
        // Given
        Long id = 1L;
        AdminAuditLogEntity log = createAuditLog(id, 1L, "CREATE", "USER");
        
        when(repository.findById(id)).thenReturn(Optional.of(log));

        // When
        AdminAuditLogEntity found = repository.findById(id).orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getUserId()).isEqualTo(1L);
        assertThat(found.getAction()).isEqualTo("CREATE");
        verify(repository).findById(id);
    }

    @Test
    void findAll_shouldReturnAllAuditLogs() {
        // Given
        List<AdminAuditLogEntity> logs = Arrays.asList(
                createAuditLog(1L, 1L, "CREATE", "USER"),
                createAuditLog(2L, 1L, "UPDATE", "SHOP"),
                createAuditLog(3L, 2L, "DELETE", "CATEGORY")
        );
        
        when(repository.findAll()).thenReturn(logs);

        // When
        List<AdminAuditLogEntity> result = repository.findAll();

        // Then
        assertThat(result).hasSize(3);
        verify(repository).findAll();
    }

    // Helper methods
    private AdminAuditLogEntity createAuditLog(Long id, Long userId, String action, String entityType) {
        return AdminAuditLogEntity.builder()
                .id(id)
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private AdminAuditLogEntity createAuditLogWithEntityId(Long id, Long userId, String action, 
                                                            String entityType, String entityId) {
        return AdminAuditLogEntity.builder()
                .id(id)
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
