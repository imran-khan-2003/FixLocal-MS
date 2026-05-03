package com.fixlocal.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixlocal.exception.ErrorCode;
import com.fixlocal.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RazorpayGatewayClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${payment.razorpay.base-url:https://api.razorpay.com}")
    private String baseUrl;

    @Value("${payment.razorpay.key-id:}")
    private String keyId;

    @Value("${payment.razorpay.key-secret:}")
    private String keySecret;

    public RazorpayOrderResponse createOrder(long amountInPaise,
                                             String currency,
                                             String receipt,
                                             String bookingId) {
        ensureConfigured();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", basicAuth());

        Map<String, Object> body = Map.of(
                "amount", amountInPaise,
                "currency", currency,
                "receipt", receipt,
                "notes", Map.of("bookingId", bookingId)
        );

        try {
            String response = restTemplate.postForObject(
                    baseUrl + "/v1/orders",
                    new HttpEntity<>(body, headers),
                    String.class
            );

            JsonNode json = objectMapper.readTree(response);
            return RazorpayOrderResponse.builder()
                    .id(json.path("id").asText())
                    .amount(json.path("amount").asLong())
                    .currency(json.path("currency").asText("INR"))
                    .status(json.path("status").asText())
                    .build();
        } catch (Exception ex) {
            throw new PaymentException(ErrorCode.EXTERNAL_SERVICE_ERROR,
                    "Failed to create Razorpay order");
        }
    }

    public void capturePayment(String paymentId, long amountInPaise, String currency) {
        ensureConfigured();

        HttpHeaders headers = buildJsonHeaders();
        Map<String, Object> body = Map.of(
                "amount", amountInPaise,
                "currency", currency
        );

        try {
            restTemplate.postForEntity(
                    baseUrl + "/v1/payments/" + paymentId + "/capture",
                    new HttpEntity<>(body, headers),
                    String.class
            );
        } catch (Exception ex) {
            throw new PaymentException(ErrorCode.EXTERNAL_SERVICE_ERROR,
                    "Failed to capture payment via Razorpay");
        }
    }

    public void refundPayment(String paymentId, long amountInPaise) {
        ensureConfigured();

        HttpHeaders headers = buildJsonHeaders();
        Map<String, Object> body = Map.of("amount", amountInPaise);

        try {
            restTemplate.postForEntity(
                    baseUrl + "/v1/payments/" + paymentId + "/refund",
                    new HttpEntity<>(body, headers),
                    String.class
            );
        } catch (Exception ex) {
            throw new PaymentException(ErrorCode.EXTERNAL_SERVICE_ERROR,
                    "Failed to refund payment via Razorpay");
        }
    }

    public String getKeyId() {
        return keyId;
    }

    private HttpHeaders buildJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", basicAuth());
        return headers;
    }

    private String basicAuth() {
        String token = keyId + ":" + keySecret;
        return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    private void ensureConfigured() {
        if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
            throw new PaymentException(ErrorCode.BAD_REQUEST,
                    "Razorpay is not configured. Please set payment.razorpay.key-id and key-secret");
        }
    }
}
