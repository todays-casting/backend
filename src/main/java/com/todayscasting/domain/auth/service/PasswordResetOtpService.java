package com.todayscasting.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PasswordResetOtpService {

    private static final String PREFIX = "password_reset_otp:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    public String generateAndStore(String email) {
        String otp = generateOtp();
        redisTemplate.opsForValue().set(PREFIX + email, otp, TTL);
        return otp;
    }

    public boolean verifyAndInvalidate(String email, String otp) {
        String key = PREFIX + email;
        String stored = redisTemplate.opsForValue().getAndDelete(key);
        return otp.equals(stored);
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1_000_000));
    }
}