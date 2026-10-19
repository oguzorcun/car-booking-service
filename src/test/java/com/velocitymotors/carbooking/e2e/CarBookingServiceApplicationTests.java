package com.velocitymotors.carbooking.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CarBookingServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
