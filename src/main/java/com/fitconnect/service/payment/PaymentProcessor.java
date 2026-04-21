package com.fitconnect.service.payment;

/**
 * Factory Pattern — PaymentProcessor (Product Interface)
 *
 * Defines the contract that all payment processor implementations must fulfil.
 * The Factory decides which concrete processor to instantiate.
 */
public interface PaymentProcessor {

    /**
     * Processes a payment attempt.
     * Throws a runtime exception if the payment fails.
     */
    void process();
}
