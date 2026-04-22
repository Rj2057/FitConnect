package com.fitconnect.service.payment;

public class PaypalPaymentProcessor implements PaymentProcessor {

    @Override
    public void process() {
        System.out.println("Processing payment via PayPal gateway...");
    }
}
