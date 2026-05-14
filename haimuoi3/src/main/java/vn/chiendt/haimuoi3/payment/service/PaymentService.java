package vn.chiendt.haimuoi3.payment.service;

import vn.chiendt.haimuoi3.payment.dto.response.CheckoutSessionResponse;

public interface PaymentService {
    CheckoutSessionResponse createStripeSession(Long orderId, Long requesterId);
    void handleWebhook(String payload, String stripeSignature);
}
