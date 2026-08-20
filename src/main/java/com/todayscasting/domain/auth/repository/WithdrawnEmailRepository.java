package com.todayscasting.domain.auth.repository;

import com.todayscasting.domain.auth.entity.WithdrawnEmail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawnEmailRepository extends JpaRepository<WithdrawnEmail, Long> {

    boolean existsByEmailHash(String emailHash);
}
