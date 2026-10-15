package com.transit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationSmokeTest {

    @Autowired
    TestRestTemplate http;

    @Test
    void refund_end_to_end() {
        var rejected = http.postForEntity("/tickets/T-4711/refunds", Map.of("amountInCents", 0), String.class);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, rejected.getStatusCode());
        var ok = http.postForEntity("/tickets/T-4711/refunds", Map.of("amountInCents", 150), String.class);
        assertEquals(HttpStatus.OK, ok.getStatusCode());
    }
}
