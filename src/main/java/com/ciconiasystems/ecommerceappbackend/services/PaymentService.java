package com.ciconiasystems.ecommerceappbackend.services;

import com.ciconiasystems.ecommerceappbackend.entities.Order;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class PaymentService {
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public void createPaymentIntent(Long amount, String currency, Order order) throws Exception {
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amount)
                        .setCurrency(currency)
                        .build();
        PaymentIntent paymentIntent = PaymentIntent.create(params);
        order.setStripeClientSecret(paymentIntent.getClientSecret());
    }

    public PaymentIntent retrievePaymentIntent(String clientSecret) throws Exception {
        String paymentIntentId = clientSecret.split("_secret")[0];
        return PaymentIntent.retrieve(paymentIntentId);
    }

    public boolean isPaymentSuccessful(String clientSecret) throws Exception {
        PaymentIntent paymentIntent = retrievePaymentIntent(clientSecret);
        return "succeeded".equals(paymentIntent.getStatus());
    }
}
