package com.fitconnect.service.payment;

public class PaymentProcessorFactory {

    private PaymentProcessorFactory() {
    }

    public static PaymentProcessor getProcessor(String type) {
        if (type == null) {
            return new MockPaymentProcessor();
        }
        return switch (type.toUpperCase()) {
            case "STRIPE" -> new StripePaymentProcessor();
            case "PAYPAL" -> new PaypalPaymentProcessor();
            case "RAZORPAY" -> new RazorpayPaymentProcessor();
            default       -> new MockPaymentProcessor();
        };
    }
}
