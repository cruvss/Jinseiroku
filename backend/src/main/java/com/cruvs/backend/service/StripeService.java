package com.cruvs.backend.service;

import com.cruvs.backend.dto.stripe.StripeRequest;
import com.cruvs.backend.dto.stripe.StripeResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StripeService {
    @Value("${stripe.secret-key}")
    private String secretKey;

    public StripeResponse checkoutProducts(StripeRequest request){
        Stripe.apiKey = secretKey;

        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(request.getName())
                .build();
        SessionCreateParams.LineItem.PriceData priceData  = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(request.getCurrency() == null ? "USD" : request.getCurrency())
                .setUnitAmount(request.getAmount())
                .setProductData(productData)
                .build();
        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(request.getQuantity())
                .setPriceData(priceData)
                .build();
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://vault.sachinkoirala.com.np")
                .setCancelUrl("https://vault.sachinkoirala.com.np")
                .addLineItem(lineItem)
                .build();

        Session session = null;

        try{
            session = Session.create(params);
        }catch (StripeException e){
            log.info("Cannot Create Session {}",e.getMessage());
        }

        return StripeResponse.builder()
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .build();
    }
}