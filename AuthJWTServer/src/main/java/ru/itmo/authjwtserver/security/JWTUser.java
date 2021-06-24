package ru.itmo.authjwtserver.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.Collection;

public class JWTUser extends AbstractAuthenticationToken {
    private final Instant createdAt;
    private final String username;
    private final Collection<GrantedAuthority> authorities;

    public JWTUser(Instant createdAt, String username, Collection<GrantedAuthority> authorities) {
        super(authorities);
        this.createdAt = createdAt;
        this.username = username;
        this.authorities = authorities;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Instant getDetails() {
        return createdAt;
    }

    @Override
    public String getPrincipal() {
        return username;
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return username;
    }
}
