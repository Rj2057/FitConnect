package com.fitconnect.service.payment;

/**
 * Factory Pattern — PaymentProcessorFactory (Factory)
 *
 * Decides which concrete PaymentProcessor to instantiate based on
 * the requested payment type. The caller (PaymentService) is completely
 * decoupled from the concrete processor classes.
 *
 * To add a new payment gateway (e.g. Razorpay), simply:
 *   1. Create RazorpayPaymentProcessor implements PaymentProcessor
 *   2. Add case "RAZORPAY" -> new RazorpayPaymentProcessor() here
 */
public class PaymentProcessorFactory {

    private PaymentProcessorFactory() {
        // Utility class — not instantiable
    }

    /**
     * Returns the appropriate PaymentProcessor for the given type.
     *
     * @param type Payment gateway type (e.g. "MOCK", "STRIPE")
     * @return Concrete PaymentProcessor instance
     */
    public static PaymentProcessor getProcessor(String type) {
        if (type == null) {
            return new MockPaymentProcessor();
        }
        return switch (type.toUpperCase()) {
            case "STRIPE" -> new StripePaymentProcessor();
            default       -> new MockPaymentProcessor();
        };
    }
}
