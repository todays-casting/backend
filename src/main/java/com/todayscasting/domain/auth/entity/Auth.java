package com.todayscasting.domain.auth.entity;

import com.todayscasting.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "auth", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "provider"})
})
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_user_id", length = 100)
    private String providerUserId;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    public enum Provider {
        LOCAL, KAKAO
    }

    public Auth(User user, Provider provider, String passwordHash) {
        this.user = user;
        this.provider = provider;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    public Auth(User user, Provider provider, String passwordHash, String providerUserId) {
        this.user = user;
        this.provider = provider;
        this.passwordHash = passwordHash;
        this.providerUserId = providerUserId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}