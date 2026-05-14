package vn.chiendt.haimuoi3.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.chiendt.haimuoi3.order.dto.request.CreateOrderRequest;
import vn.chiendt.haimuoi3.order.dto.response.OrderItemResponse;
import vn.chiendt.haimuoi3.order.dto.response.OrderResponse;
import vn.chiendt.haimuoi3.order.model.postgres.OrderEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderItemEntity;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "stripeSessionId", ignore = true)
    @Mapping(target = "checkoutBatchId", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "customerId", source = "customerId")
    OrderEntity toEntity(CreateOrderRequest request, Long customerId);

    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    @Mapping(
            target = "checkoutBatchId",
            expression = "java(entity.getCheckoutBatchId() != null ? entity.getCheckoutBatchId().toString() : null)")
    OrderResponse toResponse(OrderEntity entity);

    @Mapping(target = "unitPrice", source = "unitPrice")
    OrderItemResponse toItemResponse(OrderItemEntity item);
}
