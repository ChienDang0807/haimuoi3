package vn.chiendt.haimuoi3.payment.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

            log.info("Stripe checkout session created for orderId={}, sessionId={}", orderId, session.getId());
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

        log.info("Stripe webhook received: eventId={}, type={}", event.getId(), event.getType());

        switch (event.getType()) {
            case "checkout.session.completed" -> handleSessionCompleted(event);
            case "checkout.session.expired" -> handleSessionExpired(event);
            default -> log.debug("Unhandled Stripe event type: {}", event.getType());
        }
    }

    private void handleSessionCompleted(Event event) {
        resolveCheckoutSession(event).ifPresentOrElse(
                session -> {
                    String orderId = session.getMetadata() != null ? session.getMetadata().get("orderId") : null;
                    if (orderId == null || orderId.isBlank()) {
                        log.warn(
                                "checkout.session.completed missing orderId metadata: eventId={}, sessionId={}",
                                event.getId(),
                                session.getId()
                        );
                        return;
                    }
                    log.info(
                            "checkout.session.completed resolved: eventId={}, sessionId={}, orderId={}",
                            event.getId(),
                            session.getId(),
                            orderId
                    );
                    updateOrderStatus(Long.parseLong(orderId), session.getId(), OrderStatus.PAID);
                },
                () -> log.warn("checkout.session.completed could not be resolved: eventId={}", event.getId())
        );
    }

    private void handleSessionExpired(Event event) {
        resolveCheckoutSession(event).ifPresentOrElse(
                session -> {
                    String orderId = session.getMetadata() != null ? session.getMetadata().get("orderId") : null;
                    if (orderId == null || orderId.isBlank()) {
                        log.warn(
                                "checkout.session.expired missing orderId metadata: eventId={}, sessionId={}",
                                event.getId(),
                                session.getId()
                        );
                        return;
                    }
                    log.info(
                            "checkout.session.expired resolved: eventId={}, sessionId={}, orderId={}",
                            event.getId(),
                            session.getId(),
                            orderId
                    );
                    updateOrderStatus(Long.parseLong(orderId), session.getId(), OrderStatus.PAYMENT_FAILED);
                },
                () -> log.warn("checkout.session.expired could not be resolved: eventId={}", event.getId())
        );
    }

    private Optional<Session> resolveCheckoutSession(Event event) {
        var deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            return Optional.of((Session) deserializer.getObject().get());
        }

        String sessionId = extractCheckoutSessionId(deserializer.getRawJson());
        if (sessionId == null) {
            return Optional.empty();
        }

        try {
            Session session = Session.retrieve(sessionId);
            log.info(
                    "Retrieved checkout session from Stripe API: eventId={}, sessionId={}",
                    event.getId(),
                    session.getId()
            );
            return Optional.of(session);
        } catch (StripeException ex) {
            log.error("Failed to retrieve checkout session for event {}: {}", event.getId(), ex.getMessage());
            return Optional.empty();
        }
    }

    private String extractCheckoutSessionId(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return null;
        }
        try {
            JsonNode idNode = OBJECT_MAPPER.readTree(rawJson).get("id");
            return idNode != null && !idNode.isNull() ? idNode.asText() : null;
        } catch (Exception ex) {
            log.warn("Failed to parse checkout session id from raw JSON: {}", ex.getMessage());
            return null;
        }
    }

    private void updateOrderStatus(Long orderId, String sessionId, OrderStatus newStatus) {
        Optional<OrderEntity> optionalOrder = orderRepository.findByIdWithItems(orderId);
        if (optionalOrder.isEmpty()) {
            log.warn("Stripe webhook could not find order {} for session {}", orderId, sessionId);
            return;
        }
        OrderEntity order = optionalOrder.get();
        if (newStatus == OrderStatus.PAID) {
            if (order.getStatus() == OrderStatus.PAID) {
                log.info("Stripe webhook ignored duplicate PAID for order {}", orderId);
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
                log.info("Stripe webhook ignored duplicate PAYMENT_FAILED for order {}", orderId);
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
