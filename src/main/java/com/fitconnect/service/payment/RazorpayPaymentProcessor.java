package com.fitconnect.service.payment;

public class RazorpayPaymentProcessor implements PaymentProcessor {

    @Override
    public void process() {
        System.out.println("Processing payment via Razorpay gateway...");
    }
}
