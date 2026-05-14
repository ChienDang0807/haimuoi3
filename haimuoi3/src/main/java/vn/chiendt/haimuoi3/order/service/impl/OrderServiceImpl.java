package vn.chiendt.haimuoi3.order.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.common.constants.Constants;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.common.utils.ShopIdUtils;
import vn.chiendt.haimuoi3.notification.dto.NotificationDTO;
import vn.chiendt.haimuoi3.notification.dto.OrderNotificationPayload;
import vn.chiendt.haimuoi3.notification.model.NotificationType;
import vn.chiendt.haimuoi3.inventory.service.InventoryService;
import vn.chiendt.haimuoi3.notification.service.NotificationService;
import vn.chiendt.haimuoi3.cart.model.mongo.CartEntity;
import vn.chiendt.haimuoi3.cart.model.mongo.CartItemEntity;
import vn.chiendt.haimuoi3.cart.model.mongo.CartState;
import vn.chiendt.haimuoi3.cart.repository.CartRepository;
import vn.chiendt.haimuoi3.cart.service.CartService;
import vn.chiendt.haimuoi3.order.dto.request.CheckoutOrderRequest;
import vn.chiendt.haimuoi3.order.dto.request.CreateOrderRequest;
import vn.chiendt.haimuoi3.order.dto.request.OrderItemRequest;
import vn.chiendt.haimuoi3.order.dto.request.UpdateOrderStatusRequest;
import vn.chiendt.haimuoi3.order.dto.response.CheckoutBatchResponse;
import vn.chiendt.haimuoi3.order.dto.response.OrderResponse;
import vn.chiendt.haimuoi3.order.validator.CheckoutOrderRequestValidator;
import vn.chiendt.haimuoi3.product.model.mongo.ProductEntity;
import vn.chiendt.haimuoi3.product.model.ProductKind;
import vn.chiendt.haimuoi3.product.repository.ProductRepository;
import vn.chiendt.haimuoi3.order.mapper.OrderMapper;
import vn.chiendt.haimuoi3.order.model.postgres.OrderEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderItemEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;
import vn.chiendt.haimuoi3.order.repository.OrderRepository;
import vn.chiendt.haimuoi3.order.service.OrderService;
import vn.chiendt.haimuoi3.shop.dto.response.ShopResponse;
import vn.chiendt.haimuoi3.shop.service.ShopService;
import vn.chiendt.haimuoi3.order.validator.CreateOrderRequestValidator;
import vn.chiendt.haimuoi3.order.validator.CustomerOrderCancelValidator;
import vn.chiendt.haimuoi3.order.validator.CustomerOrderConfirmDeliveredValidator;
import vn.chiendt.haimuoi3.order.validator.OrderPageableValidator;
import vn.chiendt.haimuoi3.order.validator.ShopOrderStatusTransitionValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final NotificationService notificationService;
    private final CreateOrderRequestValidator createOrderRequestValidator;
    private final OrderPageableValidator orderPageableValidator;
    private final CustomerOrderCancelValidator customerOrderCancelValidator;
    private final CustomerOrderConfirmDeliveredValidator customerOrderConfirmDeliveredValidator;
    private final ShopService shopService;
    private final ShopOrderStatusTransitionValidator shopOrderStatusTransitionValidator;
    private final InventoryService inventoryService;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final CheckoutOrderRequestValidator checkoutOrderRequestValidator;

    @Override
    @Transactional
    public OrderResponse createOrder(Long customerId, CreateOrderRequest request) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("customer context is invalid");
        }
        return placeOrder(customerId, request, null);
    }

    @Override
    @Transactional
    public CheckoutBatchResponse checkoutFromCart(Long customerId, CheckoutOrderRequest request) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("customer context is invalid");
        }
        checkoutOrderRequestValidator.validate(request);
        CartEntity cart = cartRepository.findByUserIdAndState(String.valueOf(customerId), CartState.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User cart not found"));
        if (!cart.getId().equals(request.getCartId())) {
            throw new IllegalArgumentException("cartId does not match active cart");
        }
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("cart is empty");
        }
        UUID batchId = UUID.randomUUID();
        Map<Long, List<CartItemEntity>> linesByShop = new TreeMap<>();
        for (CartItemEntity line : cart.getItems()) {
            long shopPk = resolveShopPkForCartLine(line);
            linesByShop.computeIfAbsent(shopPk, k -> new ArrayList<>()).add(line);
        }
        List<OrderResponse> created = new ArrayList<>();
        for (Map.Entry<Long, List<CartItemEntity>> entry : linesByShop.entrySet()) {
            CreateOrderRequest perShop = buildCreateOrderRequestForShop(entry.getKey(), request, entry.getValue());
            created.add(placeOrder(customerId, perShop, batchId));
        }
        cartService.clearUserCart(customerId);
        return CheckoutBatchResponse.builder()
                .checkoutBatchId(batchId.toString())
                .orders(created)
                .build();
    }

    private OrderResponse placeOrder(Long customerId, CreateOrderRequest request, UUID checkoutBatchId) {
        createOrderRequestValidator.validate(request);
        inventoryService.assertAvailabilityForCreateOrder(request);
        OrderEntity order = orderMapper.toEntity(request, customerId);
        order.setCheckoutBatchId(checkoutBatchId);

        boolean isStripe = "STRIPE".equalsIgnoreCase(request.getPaymentMethod());
        order.setStatus(isStripe ? OrderStatus.PENDING_PAYMENT : OrderStatus.CONFIRMED);
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "COD");
        order.setCreatedAt(LocalDateTime.now());

        if (request.getItems() != null) {
            List<OrderItemEntity> items = request.getItems().stream()
                    .map(itemReq -> OrderItemEntity.builder()
                            .order(order)
                            .productId(itemReq.getProductId())
                            .productName(itemReq.getProductName())
                            .unitPrice(itemReq.getUnitPrice())
                            .quantity(itemReq.getQuantity())
                            .subtotal(itemReq.getSubtotal())
                            .build())
                    .collect(Collectors.toList());
            order.setItems(items);
        }

        OrderEntity savedOrder = orderRepository.save(order);
        inventoryService.applyAfterOrderPlaced(savedOrder);

        OrderNotificationPayload payload = OrderNotificationPayload.builder()
                .orderId(savedOrder.getId())
                .shopId(savedOrder.getShopId())
                .customerName(savedOrder.getCustomerName())
                .totalAmount(savedOrder.getTotalAmount())
                .status(savedOrder.getStatus().name())
                .createdAt(savedOrder.getCreatedAt())
                .build();

        NotificationDTO notification = NotificationDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(NotificationType.ORDER_CREATED)
                .recipientRole("SHOP_OWNER")
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        notificationService.sendToShop(savedOrder.getShopId(), notification);

        return orderMapper.toResponse(savedOrder);
    }

    private long resolveShopPkForCartLine(CartItemEntity line) {
        if (line.getShopId() != null && !line.getShopId().isBlank()) {
            return ShopIdUtils.requireLongShopId(
                    line.getShopId(), "invalid shopId on cart line for product: " + line.getProductId());
        }
        ProductEntity product = productRepository.findById(line.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + line.getProductId()));
        return parseShopIdFromProduct(product, line.getProductId());
    }

    private long parseShopIdFromProduct(ProductEntity product, String productIdForMessage) {
        if (product.getShopId() == null || product.getShopId().isBlank()) {
            throw new IllegalArgumentException("Product has no shop: " + productIdForMessage);
        }
        return ShopIdUtils.requireLongShopId(
                product.getShopId(), "Product shopId is not numeric: " + productIdForMessage);
    }

    private CreateOrderRequest buildCreateOrderRequestForShop(
            long shopId,
            CheckoutOrderRequest checkout,
            List<CartItemEntity> lines) {
        List<OrderItemRequest> itemRequests = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (CartItemEntity line : lines) {
            ProductEntity product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + line.getProductId()));
            String status = product.getStatus();
            if (status == null || !"ACTIVE".equalsIgnoreCase(status.trim())) {
                throw new IllegalArgumentException("Product is not available: " + line.getProductId());
            }
            if (ProductKind.resolve(product) == ProductKind.PARENT) {
                throw new IllegalArgumentException(Constants.Product.PARENT_NOT_SELLABLE);
            }
            long productShop = parseShopIdFromProduct(product, line.getProductId());
            if (productShop != shopId) {
                throw new IllegalArgumentException("Cart line shop mismatch for product: " + line.getProductId());
            }
            String name = line.getProductNameSnapshot() != null && !line.getProductNameSnapshot().isBlank()
                    ? line.getProductNameSnapshot()
                    : product.getName();
            BigDecimal unit = line.getUnitPriceSnapshot() != null ? line.getUnitPriceSnapshot() : product.getPrice();
            if (unit == null || unit.signum() < 0) {
                throw new IllegalArgumentException("Invalid unit price for product: " + line.getProductId());
            }
            BigDecimal subtotal = unit.multiply(BigDecimal.valueOf(line.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            total = total.add(subtotal);
            itemRequests.add(OrderItemRequest.builder()
                    .productId(line.getProductId())
                    .productName(name != null ? name : line.getProductId())
                    .unitPrice(unit.setScale(2, RoundingMode.HALF_UP))
                    .quantity(line.getQuantity())
                    .subtotal(subtotal)
                    .build());
        }
        return CreateOrderRequest.builder()
                .shopId(shopId)
                .customerName(checkout.getCustomerName())
                .totalAmount(total)
                .paymentMethod(checkout.getPaymentMethod())
                .shippingAddress(checkout.getShippingAddress())
                .items(itemRequests)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderForCustomer(Long customerId, Long orderId) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("customer context is invalid");
        }
        createOrderRequestValidator.validatePositiveOrderId(orderId);
        OrderEntity order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByShopId(Long shopId) {
        return getOrdersByShopId(shopId, Pageable.unpaged()).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByShopId(Long shopId, Pageable pageable) {
        orderPageableValidator.validate(pageable);
        return orderRepository.findByShopId(shopId, pageable).map(orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        return getOrdersByCustomerId(customerId, Pageable.unpaged()).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        orderPageableValidator.validate(pageable);
        return orderRepository.findByCustomerId(customerId, pageable).map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return applyOrderStatusUpdate(order, request.getStatus());
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatusByShopOwner(Long ownerUserId, Long orderId, UpdateOrderStatusRequest request) {
        if (ownerUserId == null || ownerUserId <= 0) {
            throw new IllegalArgumentException("owner context is invalid");
        }
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        createOrderRequestValidator.validatePositiveOrderId(orderId);
        ShopResponse shop = shopService.getShopByOwnerId(ownerUserId);
        OrderEntity order = orderRepository.findByIdAndShopId(orderId, shop.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        shopOrderStatusTransitionValidator.validateTransition(
                order.getStatus(),
                request.getStatus(),
                order.getPaymentMethod()
        );
        return applyOrderStatusUpdate(order, request.getStatus());
    }

    private OrderResponse applyOrderStatusUpdate(OrderEntity order, OrderStatus newStatus) {
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        OrderEntity updatedOrder = orderRepository.save(order);
        notifyCustomerOrderStatus(updatedOrder);
        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelMyOrder(Long customerId, Long orderId) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("customer context is invalid");
        }
        createOrderRequestValidator.validatePositiveOrderId(orderId);
        OrderEntity order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        customerOrderCancelValidator.validateCurrentStatus(order.getStatus());
        OrderStatus previousStatus = order.getStatus();
        inventoryService.restockAfterCustomerCancelConfirmedCod(
                order.getId(), previousStatus, order.getPaymentMethod());
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        OrderEntity saved = orderRepository.save(order);
        notifyCustomerOrderStatus(saved);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse confirmDeliveredMyOrder(Long customerId, Long orderId) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("customer context is invalid");
        }
        createOrderRequestValidator.validatePositiveOrderId(orderId);
        OrderEntity order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        customerOrderConfirmDeliveredValidator.validateCurrentStatus(order.getStatus());
        order.setStatus(OrderStatus.DELIVERED);
        order.setUpdatedAt(LocalDateTime.now());
        OrderEntity saved = orderRepository.save(order);
        notifyCustomerOrderStatus(saved);
        return orderMapper.toResponse(saved);
    }

    private void notifyCustomerOrderStatus(OrderEntity updatedOrder) {
        OrderNotificationPayload payload = OrderNotificationPayload.builder()
                .orderId(updatedOrder.getId())
                .shopId(updatedOrder.getShopId())
                .customerName(updatedOrder.getCustomerName())
                .totalAmount(updatedOrder.getTotalAmount())
                .status(updatedOrder.getStatus().name())
                .createdAt(updatedOrder.getCreatedAt())
                .build();

        NotificationType notificationType = mapStatusToNotificationType(updatedOrder.getStatus());

        NotificationDTO notification = NotificationDTO.builder()
                .id(UUID.randomUUID().toString())
                .type(notificationType)
                .recipientId(updatedOrder.getCustomerId())
                .recipientRole("CUSTOMER")
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        notificationService.sendToUser(updatedOrder.getCustomerId(), notification);
    }

    private NotificationType mapStatusToNotificationType(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> NotificationType.ORDER_CONFIRMED;
            case PAID -> NotificationType.ORDER_PAID;
            case READY_TO_SHIP -> NotificationType.ORDER_UPDATED;
            case PAYMENT_FAILED -> NotificationType.ORDER_PAYMENT_FAILED;
            case SHIPPING -> NotificationType.ORDER_SHIPPING;
            case DELIVERED -> NotificationType.ORDER_DELIVERED;
            case CANCELLED -> NotificationType.ORDER_CANCELLED;
            default -> NotificationType.ORDER_UPDATED;
        };
    }
}
