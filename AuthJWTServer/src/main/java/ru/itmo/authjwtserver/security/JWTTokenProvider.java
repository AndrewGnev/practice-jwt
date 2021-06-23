package ru.itmo.authjwtserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import ru.itmo.authjwtserver.security.refresh.RefreshTokenService;
import ru.itmo.authjwtserver.user.model.Role;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JWTTokenProvider {

    @Value("${jwt.token.secret}")
    private String secret;

    @Value("${jwt.token.expired}")
    private long validityTime;

    private RefreshTokenService refreshService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @PostConstruct
    protected void init() { secret = Base64.getEncoder().encodeToString(secret.getBytes()); }

    public String createAccessToken(String username, Set<Role> roles) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(roles);

        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", getRoleNames(roles));

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String createRefreshToken(String username) {
        return refreshService.generateRefreshToken(username).getToken();
    }

    private List<String> getRoleNames(Set<Role> roles) {
        return roles.stream().map(Enum::name).collect(Collectors.toList());
    }

    @Autowired
    public void setRefreshService(RefreshTokenService refreshService) {
        this.refreshService = refreshService;
    }
}
