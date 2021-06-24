package ru.itmo.authjwtserver.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import ru.itmo.authjwtserver.security.refresh.RefreshTokenService;
import ru.itmo.authjwtserver.user.model.Role;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
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

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

        return new JWTUser(claims.getIssuedAt().toInstant(), claims.getSubject(),
                ((Collection<String>) claims.get("roles")).stream()
                        .map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
    }

    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(6).trim();
        }

        return null;
    }

    public boolean validateAccessToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            throw new JWTAuthenticationException("JWT token is expired or invalid");
        }
    }

    @Autowired
    public void setRefreshService(RefreshTokenService refreshService) {
        this.refreshService = refreshService;
    }
}
