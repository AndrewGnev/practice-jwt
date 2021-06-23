package ru.itmo.apiserver.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class JWTAuthenticationProvider implements AuthenticationProvider {

    private final JWTTokenProvider jwtTokenProvider;

    public JWTAuthenticationProvider(JWTTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        if (authentication instanceof JWTUser) {

            if (jwtTokenProvider.validateAccessToken(((JWTUser) authentication).getToken())) {
                return authentication;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(JWTUser.class);
    }
}
