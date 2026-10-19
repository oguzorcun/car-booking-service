package com.velocitymotors.carbooking.unit;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.velocitymotors.carbooking.client.CreditCardClient;
import com.velocitymotors.carbooking.exception.CreditCardPaymentRejectedException;
import com.velocitymotors.carbooking.generated.creditcard.ApiClient;
import com.velocitymotors.carbooking.generated.creditcard.api.PaymentStatusApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreditCardClientTest {
    /**
     * Serves the same wiremock stubs as docker-compose.yaml
     */
    @RegisterExtension
    static final WireMockExtension WIREMOCK = WireMockExtension.newInstance()
            .options(options().dynamicPort().usingFilesUnderDirectory("wiremock"))
            .build();

    private CreditCardClient client;

    @BeforeEach
    void setUp() {
        ApiClient apiClient = new ApiClient().setBasePath(WIREMOCK.baseUrl() + "/host/credit-card-api");
        client = new CreditCardClient(new PaymentStatusApi(apiClient));
    }

    @Test
    void returnsTrueWhenPaymentIsApproved() {
        assertThat(client.isPaymentApproved("PAY-REF-1")).isTrue();
    }

    @Test
    void returnsFalseWhenPaymentIsRejected() {
        assertThat(client.isPaymentApproved("REJECT-1")).isFalse();
    }

    @Test
    void throwsWhenCreditCardServiceFails() {
        assertThatThrownBy(() -> client.isPaymentApproved("FAIL-1"))
                .isInstanceOf(CreditCardPaymentRejectedException.class);
    }
}
