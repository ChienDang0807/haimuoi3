package vn.chiendt.haimuoi3.notification.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.chiendt.haimuoi3.notification.dto.NotificationDTO;
import vn.chiendt.haimuoi3.notification.model.postgres.NotificationEntity;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "id", expression = "java(entity.getId().toString())")
    @Mapping(target = "type", source = "notificationType")
    @Mapping(target = "timestamp", source = "createdAt")
    NotificationDTO toDto(NotificationEntity entity);
}
