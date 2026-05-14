package vn.chiendt.haimuoi3.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.chiendt.haimuoi3.common.dto.ApiResponse;
import vn.chiendt.haimuoi3.payment.dto.request.CreateCheckoutSessionRequest;
import vn.chiendt.haimuoi3.payment.dto.response.CheckoutSessionResponse;
import vn.chiendt.haimuoi3.payment.service.PaymentService;
import vn.chiendt.haimuoi3.user.model.postgres.UserEntity;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/stripe/checkout-session")
    public ApiResponse<CheckoutSessionResponse> createCheckoutSession(
            @RequestBody CreateCheckoutSessionRequest request,
            @AuthenticationPrincipal UserEntity currentUser) {
        CheckoutSessionResponse response = paymentService.createStripeSession(
                request.getOrderId(), currentUser.getId());
        return ApiResponse.success(response, "Stripe checkout session created");
    }

    @PostMapping("/webhook")
    public ApiResponse<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String stripeSignature) {
        paymentService.handleWebhook(payload, stripeSignature);
        return ApiResponse.success(null, "Webhook processed");
    }
}
