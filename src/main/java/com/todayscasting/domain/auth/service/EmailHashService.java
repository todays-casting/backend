package com.todayscasting.domain.auth.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;

@Component
public class EmailHashService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec secretKeySpec;

    public EmailHashService(@Value("${app.auth.withdrawn-email-hmac-secret}") String secret) {
        this.secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
    }

    public String hash(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hashedEmail = mac.doFinal(normalizedEmail.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedEmail);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to hash withdrawn email", exception);
        }
    }
}
