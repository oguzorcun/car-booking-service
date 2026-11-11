package com.velocitymotors.carbooking.exception;

public class CreditCardPaymentRejectedException extends RuntimeException {

    public CreditCardPaymentRejectedException(String message) {
        super(message);
    }
}
