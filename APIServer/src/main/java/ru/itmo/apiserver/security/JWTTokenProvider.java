package ru.itmo.apiserver.security;


import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import ru.itmo.apiserver.user.model.Role;

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

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @PostConstruct
    protected void init() { secret = Base64.getEncoder().encodeToString(secret.getBytes()); }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

        JWTUser user = new JWTUser(token, claims.getIssuedAt().toInstant(), claims.getSubject(),
                ((Collection<String>) claims.get("roles")).stream()
                .map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
        return new UsernamePasswordAuthenticationToken(user, user, user.getAuthorities());
    }

    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
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

    private List<String> getRoleNames(Set<Role> roles) {
        return roles.stream().map(Enum::name).collect(Collectors.toList());
    }
}

