package com.cruvs.backend.controller;

import com.cruvs.backend.dto.stripe.StripeRequest;
import com.cruvs.backend.dto.stripe.StripeResponse;
import com.cruvs.backend.response.ApiResponse;
import com.cruvs.backend.service.StripeService;
import com.cruvs.backend.util.ApiResponseUtil;
import com.cruvs.backend.util.GetAuthUser;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payment")
public class PaymentController {
    private final StripeService stripeService;
    private final GetAuthUser getAuthUser;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<StripeResponse>> checkout(@RequestBody StripeRequest request){
        StripeResponse stripeResponse = stripeService.checkoutProducts(getAuthUser.getAuthenticatedUserId(), request);
        return ResponseEntity.ok(ApiResponseUtil.success("Payment Session Created", stripeResponse));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyPayment(@RequestParam("session_id") String sessionId) throws com.stripe.exception.StripeException {
        stripeService.verifyCheckoutSession(sessionId);
        return ResponseEntity.ok(ApiResponseUtil.success("Payment verified and plan updated successfully", null));
    }

}
