package com.todayscasting.domain.notification.repository;

import com.todayscasting.domain.notification.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    Optional<UserFcmToken> findByToken(String token);

    List<UserFcmToken> findByUserIdAndDeletedAtIsNull(Long userId);
}
