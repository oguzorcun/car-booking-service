package com.velocitymotors.carbooking.client;

import com.velocitymotors.carbooking.exception.CreditCardPaymentRejectedException;
import com.velocitymotors.carbooking.generated.creditcard.api.PaymentStatusApi;
import com.velocitymotors.carbooking.generated.creditcard.model.PaymentStatus;
import com.velocitymotors.carbooking.generated.creditcard.model.PaymentStatusResponse;
import com.velocitymotors.carbooking.generated.creditcard.model.PaymentStatusRetrievalRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class CreditCardClient {

    private final PaymentStatusApi paymentStatusApi;

    public boolean isPaymentApproved(String paymentReference) {
        PaymentStatusRetrievalRequest request =
                new PaymentStatusRetrievalRequest().paymentReference(paymentReference);
        try {
            PaymentStatusResponse response = paymentStatusApi.paymentStatusPost(request);
            return response != null && response.getStatus() == PaymentStatus.APPROVED;
        } catch (RestClientException ex) {
            throw new CreditCardPaymentRejectedException(
                    "Credit card payment validation failed for reference " + paymentReference);
        }
    }
}
