package ru.itmo.authjwtserver.user.api;

public class AuthenticationResponseDto {

    private final String accessToken;
    private final String refreshToken;

    public AuthenticationResponseDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
