package com.scrapspringboot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "verification_token", schema = "indexer")
public class VerificationToken {

    private static final int EXPIRATION_HOURS = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = Usuario.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public VerificationToken(String token, Usuario usuario) {
        this.token = token;
        this.usuario = usuario;
        this.expiryDate = calculateExpiryDate(EXPIRATION_HOURS);
    }

    private LocalDateTime calculateExpiryDate(int expiryTimeInHours) {
        return LocalDateTime.now().plusHours(expiryTimeInHours);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}