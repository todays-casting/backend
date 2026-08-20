package com.todayscasting.domain.auth.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailHashServiceTest {

    @Test
    void hashesNormalizedEmailWithHmacSha256() {
        EmailHashService emailHashService = new EmailHashService("secret-key-for-test");

        String hash = emailHashService.hash(" Old@Example.com ");

        assertThat(hash).hasSize(64);
        assertThat(hash).isEqualTo(emailHashService.hash("old@example.com"));
    }

    @Test
    void hashChangesWhenSecretChanges() {
        EmailHashService firstService = new EmailHashService("first-secret-key");
        EmailHashService secondService = new EmailHashService("second-secret-key");

        assertThat(firstService.hash("old@example.com"))
                .isNotEqualTo(secondService.hash("old@example.com"));
    }
}
