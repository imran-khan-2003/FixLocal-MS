package com.fixlocal.gateway;

import com.fixlocal.exception.ErrorCode;
import com.fixlocal.exception.PaymentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class RazorpaySignatureService {

    @Value("${payment.razorpay.key-secret:}")
    private String keySecret;

    @Value("${payment.razorpay.webhook-secret:}")
    private String webhookSecret;

    public boolean isPaymentSignatureValid(String orderId, String paymentId, String signature) {
        ensureKeySecretConfigured();
        String payload = orderId + "|" + paymentId;
        String expected = hmacSha256Hex(payload, keySecret);
        return safeEquals(expected, signature);
    }

    public boolean isWebhookSignatureValid(String payload, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            return false;
        }
        String expected = hmacSha256Hex(payload, webhookSecret);
        return safeEquals(expected, signature);
    }

    private void ensureKeySecretConfigured() {
        if (keySecret == null || keySecret.isBlank()) {
            throw new PaymentException(ErrorCode.BAD_REQUEST,
                    "Razorpay key secret is missing");
        }
    }

    private String hmacSha256Hex(String payload, String secret) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(keySpec);
            byte[] digest = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new PaymentException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Unable to validate Razorpay signature");
        }
    }

    private boolean safeEquals(String expected, String actual) {
        if (expected == null || actual == null) return false;
        if (expected.length() != actual.length()) return false;
        int result = 0;
        for (int i = 0; i < expected.length(); i++) {
            result |= expected.charAt(i) ^ actual.charAt(i);
        }
        return result == 0;
    }
}
