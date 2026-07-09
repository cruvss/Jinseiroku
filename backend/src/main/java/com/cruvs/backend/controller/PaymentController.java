package com.cruvs.backend.controller;

import com.cruvs.backend.dto.stripe.StripeRequest;
import com.cruvs.backend.dto.stripe.StripeResponse;
import com.cruvs.backend.response.ApiResponse;
import com.cruvs.backend.service.StripeService;
import com.cruvs.backend.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payment")
public class PaymentController {
    private final StripeService stripeService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<StripeResponse>> checkout(@RequestBody StripeRequest request){
        StripeResponse stripeResponse = stripeService.checkoutProducts(request);

        return ResponseEntity.ok(ApiResponseUtil.success("Payment Session Created",stripeResponse));
    }

}
