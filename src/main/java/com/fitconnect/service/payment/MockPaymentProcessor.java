package com.fitconnect.service.payment;

import com.fitconnect.exception.PaymentException;
import java.util.Random;

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
