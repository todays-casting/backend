package com.todayscasting.domain.auth.repository;

import com.todayscasting.domain.auth.entity.Auth;
import com.todayscasting.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Auth, Long> {

    Optional<Auth> findByUserAndProvider(User user, Auth.Provider provider);
    Optional<Auth> findByProviderAndProviderUserId(Auth.Provider provider, String providerUserId);
}