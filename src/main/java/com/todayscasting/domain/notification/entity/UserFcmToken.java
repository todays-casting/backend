package com.todayscasting.domain.notification.entity;

import com.todayscasting.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_fcm_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 512)
    private String token;

    public static UserFcmToken create(Long userId, String token) {
        UserFcmToken userFcmToken = new UserFcmToken();
        userFcmToken.userId = userId;
        userFcmToken.token = token;
        return userFcmToken;
    }

    public void updateOwner(Long userId) {
        this.userId = userId;
    }
}
