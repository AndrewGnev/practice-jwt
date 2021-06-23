package ru.itmo.authjwtserver.security.refresh;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.authjwtserver.user.UserService;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(UserService userService, RefreshTokenRepository repository) {
        this.userService = userService;
        this.refreshTokenRepository = repository;
    }

    public RefreshToken generateRefreshToken(String username) {
        RefreshToken refreshToken = refreshTokenRepository.findByUser_Username(username)
                .orElseGet(() -> {
                    RefreshToken token = new RefreshToken();
                    token.setUser(userService.getByUsername(username));
                    return token;
                });

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setCreatedDate(Instant.now());
        refreshToken.setValidUntil(Instant.now().plusSeconds(3600));

        return refreshTokenRepository.save(refreshToken);
    }

    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    public RefreshToken getRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token).orElse(null);
    }
}
