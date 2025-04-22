package com.scrapspringboot.model;


import com.scrapspringboot.enums.Rol;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Collection;
import java.util.Collections;


@Entity
@Table(name = "usuario", schema = "indexer")
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Usuario implements UserDetails
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Email
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private Rol rol;

    @Transient
    private String resetToken;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.rol.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
    @Override
    public String getUsername() {
        return this.username;
    }
}


