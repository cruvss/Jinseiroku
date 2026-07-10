package com.cruvs.backend.service;

import com.cruvs.backend.dto.stripe.StripeRequest;
import com.cruvs.backend.dto.stripe.StripeResponse;
import com.cruvs.backend.entity.SubscriptionPlan;
import com.cruvs.backend.entity.User;
import com.cruvs.backend.repository.SubscriptionPlanRepository;
import com.cruvs.backend.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class StripeService {
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    @Value("${stripe.secret-key}")
    private String secretKey;

    public StripeService(UserRepository userRepository, SubscriptionPlanRepository subscriptionPlanRepository) {
        this.userRepository = userRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    public StripeResponse checkoutProducts(UUID userId, StripeRequest request){
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
                .setSuccessUrl("http://localhost:4200/payment-success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:4200/payment-cancel")
                .putMetadata("userId", userId.toString())
                .putMetadata("planId", request.getPlanId().toString())
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

    @Transactional
    public void verifyCheckoutSession(String sessionId) throws StripeException{
        Stripe.apiKey = secretKey;
        Session session = Session.retrieve(sessionId);

        if ("paid".equalsIgnoreCase(session.getPaymentStatus())){
            String userIdStr = session.getMetadata().get("userId");
            String planIdStr = session.getMetadata().get("planId");

            if (userIdStr !=null && planIdStr !=null){
                UUID userId = UUID.fromString(userIdStr);
                UUID planId = UUID.fromString(planIdStr);

                User user = userRepository.findById(userId)
                        .orElseThrow();

                SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                        .orElseThrow();

                user.setSubscriptionPlan(plan);
                userRepository.save(user);
                log.info("User {} successfully upgraded to plan {}", userId, plan.getName());
            }
        } else {
            throw new RuntimeException("Payment not completed");
        }
    }
}