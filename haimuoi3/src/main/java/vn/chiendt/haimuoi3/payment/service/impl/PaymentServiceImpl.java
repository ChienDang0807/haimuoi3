package vn.chiendt.haimuoi3.payment.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.chiendt.haimuoi3.common.exception.ResourceNotFoundException;
import vn.chiendt.haimuoi3.inventory.service.InventoryService;
import vn.chiendt.haimuoi3.order.model.postgres.OrderEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderItemEntity;
import vn.chiendt.haimuoi3.order.model.postgres.OrderStatus;
import vn.chiendt.haimuoi3.order.repository.OrderRepository;
import vn.chiendt.haimuoi3.payment.dto.response.CheckoutSessionResponse;
import vn.chiendt.haimuoi3.payment.service.PaymentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${app.base-url}")
    private String baseUrl;

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    @Transactional
    public CheckoutSessionResponse createStripeSession(Long orderId, Long requesterId) {
        OrderEntity order = orderRepository.findByIdAndCustomerId(orderId, requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException(
                    "Order cannot be paid in current status: " + order.getStatus());
        }

        try {
            List<SessionCreateParams.LineItem> lineItems = buildLineItems(order);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(baseUrl + "/order-confirmation?orderId=" + orderId + "&session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(baseUrl + "/checkout?cancelled=true")
                    .addAllLineItem(lineItems)
                    .putMetadata("orderId", orderId.toString())
                    .build();

            Session session = Session.create(params);

            order.setStripeSessionId(session.getId());
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            return new CheckoutSessionResponse(session.getUrl());
        } catch (StripeException e) {
            log.error("Failed to create Stripe session for order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to create payment session: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String stripeSignature) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, stripeSignature, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid webhook signature");
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> handleSessionCompleted(event);
            case "checkout.session.expired" -> handleSessionExpired(event);
            default -> log.debug("Unhandled Stripe event type: {}", event.getType());
        }
    }

    private void handleSessionCompleted(Event event) {
        event.getDataObjectDeserializer().getObject().ifPresent(obj -> {
            Session session = (Session) obj;
            String orderId = session.getMetadata().get("orderId");
            if (orderId != null) {
                updateOrderStatus(Long.parseLong(orderId), session.getId(), OrderStatus.PAID);
            }
        });
    }

    private void handleSessionExpired(Event event) {
        event.getDataObjectDeserializer().getObject().ifPresent(obj -> {
            Session session = (Session) obj;
            String orderId = session.getMetadata().get("orderId");
            if (orderId != null) {
                updateOrderStatus(Long.parseLong(orderId), session.getId(), OrderStatus.PAYMENT_FAILED);
            }
        });
    }

    private void updateOrderStatus(Long orderId, String sessionId, OrderStatus newStatus) {
        Optional<OrderEntity> optionalOrder = orderRepository.findByIdWithItems(orderId);
        if (optionalOrder.isEmpty()) {
            return;
        }
        OrderEntity order = optionalOrder.get();
        if (newStatus == OrderStatus.PAID) {
            if (order.getStatus() == OrderStatus.PAID) {
                return;
            }
            if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
                log.warn("Skipping PAID webhook for order {} in status {}", orderId, order.getStatus());
                return;
            }
            order.setStatus(OrderStatus.PAID);
            order.setStripeSessionId(sessionId);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            inventoryService.commitHeldForOrder(orderId);
            log.info("Order {} status updated to {} via Stripe webhook", orderId, newStatus);
            return;
        }
        if (newStatus == OrderStatus.PAYMENT_FAILED) {
            if (order.getStatus() == OrderStatus.PAYMENT_FAILED) {
                return;
            }
            if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
                log.warn("Skipping PAYMENT_FAILED webhook for order {} in status {}", orderId, order.getStatus());
                return;
            }
            inventoryService.releaseHeldForOrder(orderId);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            order.setStripeSessionId(sessionId);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            log.info("Order {} status updated to {} via Stripe webhook", orderId, newStatus);
        }
    }

    private List<SessionCreateParams.LineItem> buildLineItems(OrderEntity order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return List.of(SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("usd")
                            .setUnitAmount(order.getTotalAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue())
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Order #" + order.getId())
                                    .build())
                            .build())
                    .build());
        }

        return order.getItems().stream()
                .map(item -> SessionCreateParams.LineItem.builder()
                        .setQuantity((long) item.getQuantity())
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(100)).longValue())
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(item.getProductName())
                                        .build())
                                .build())
                        .build())
                .toList();
    }
}
