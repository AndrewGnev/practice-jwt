package ru.itmo.client;

public class AuthenticationRequestDto {

    private String username;
    private String password;

    public AuthenticationRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
