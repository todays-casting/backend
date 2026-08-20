package com.todayscasting.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "withdrawn_emails")
public class WithdrawnEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_hash", nullable = false, unique = true, columnDefinition = "CHAR(64)")
    private String emailHash;

    @Column(name = "withdrawn_at", nullable = false)
    private LocalDateTime withdrawnAt;

    public WithdrawnEmail(String emailHash) {
        this.emailHash = emailHash;
        this.withdrawnAt = LocalDateTime.now();
    }
}
