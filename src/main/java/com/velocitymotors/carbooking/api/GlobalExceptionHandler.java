package com.velocitymotors.carbooking.api;

import com.velocitymotors.carbooking.exception.BookingValidationException;
import com.velocitymotors.carbooking.exception.PaymentRejectedException;
import com.velocitymotors.carbooking.generated.booking.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(BookingValidationException exception) {
        return errorResponse(exception.getMessage());
    }

    @ExceptionHandler(PaymentRejectedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErrorResponse handle(PaymentRejectedException exception) {
        return errorResponse(exception.getMessage());
    }

    private ErrorResponse errorResponse(String message) {
        ErrorResponse response = new ErrorResponse();
        response.setError(message);
        return response;
    }
}
