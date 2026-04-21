package com.fitconnect.service.payment;

/**
 * Factory Pattern — StripePaymentProcessor (Concrete Product)
 *
 * Stub for a real Stripe payment integration.
 * In production, this would use the Stripe SDK to charge a card.
 */
public class StripePaymentProcessor implements PaymentProcessor {

    @Override
    public void process() {
        // TODO: Integrate Stripe SDK — stripe.charges.create(...)
        // For now, this stub always succeeds (used only in STRIPE payment type requests)
        System.out.println("[StripePaymentProcessor] Processing payment via Stripe gateway (stub)");
    }
}
