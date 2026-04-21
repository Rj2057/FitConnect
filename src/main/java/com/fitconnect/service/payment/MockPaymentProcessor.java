package com.fitconnect.service.payment;

import com.fitconnect.exception.PaymentException;
import java.util.Random;

/**
 * Factory Pattern — MockPaymentProcessor (Concrete Product)
 *
 * Simulates a payment gateway with an 80% success / 20% failure rate.
 * This was previously an inline private method in PaymentService.
 */
public class MockPaymentProcessor implements PaymentProcessor {

    @Override
    public void process() {
        Random random = new Random();
        int result = random.nextInt(100);
        if (result >= 80) {
            throw new PaymentException("Payment verification failed. Please try again.");
        }
    }
}
