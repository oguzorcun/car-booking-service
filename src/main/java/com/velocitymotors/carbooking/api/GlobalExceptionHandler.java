package com.velocitymotors.carbooking.api;

import com.velocitymotors.carbooking.exception.BookingValidationException;
import com.velocitymotors.carbooking.exception.CreditCardPaymentRejectedException;
import com.velocitymotors.carbooking.generated.booking.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(BookingValidationException exception) {
        return errorResponse(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(MethodArgumentNotValidException exception) {
        return errorResponse(exception.getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining(", ")));
    }

    @ExceptionHandler(CreditCardPaymentRejectedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErrorResponse handle(CreditCardPaymentRejectedException exception) {
        return errorResponse(exception.getMessage());
    }

    private ErrorResponse errorResponse(String message) {
        return new ErrorResponse().error(message);
    }
}
