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
    void refund_end_to_end_and_too_much_is_rejected() {
        var ok = http.postForEntity("/tickets/T-4711/refunds", Map.of("amountInCents", 400), String.class);
        assertEquals(HttpStatus.OK, ok.getStatusCode());
        var tooMuch = http.postForEntity("/tickets/T-4711/refunds", Map.of("amountInCents", 1), String.class);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, tooMuch.getStatusCode());
    }
}
