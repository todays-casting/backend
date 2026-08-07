package com.todayscasting.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Configuration
@ConditionalOnProperty(name = "FIREBASE_ENABLED", havingValue = "true")
public class FirebaseConfig {

    @Bean
    public FirebaseMessaging firebaseMessaging(
            @Value("${FIREBASE_PROJECT_ID:}") String projectId
    ) throws IOException {
        FirebaseApp firebaseApp = FirebaseApp.getApps().isEmpty()
                ? FirebaseApp.initializeApp(firebaseOptions(projectId))
                : FirebaseApp.getInstance();

        return FirebaseMessaging.getInstance(firebaseApp);
    }

    private FirebaseOptions firebaseOptions(String projectId) throws IOException {
        FirebaseOptions.Builder builder = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault());

        if (StringUtils.hasText(projectId)) {
            builder.setProjectId(projectId);
        }

        return builder.build();
    }
}
