package com.fitconnect.service.payment;

public class StripePaymentProcessor implements PaymentProcessor {

    @Override
    public void process() {
        System.out.println("[StripePaymentProcessor] Processing payment via Stripe gateway (stub)");
    }
}
