package ru.itmo.authjwtserver.user.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import ru.itmo.authjwtserver.security.JWTAuthenticationException;
import ru.itmo.authjwtserver.security.JWTTokenProvider;
import ru.itmo.authjwtserver.security.refresh.RefreshTokenService;
import ru.itmo.authjwtserver.user.UserService;
import ru.itmo.authjwtserver.user.model.Role;
import ru.itmo.authjwtserver.user.model.User;

import java.util.*;

@RestController
@RequestMapping("/api/auth/")
public class UserController {
    private final AuthenticationManager authenticationManager;
    private final JWTTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public UserController(AuthenticationManager authenticationManager, JWTTokenProvider jwtTokenProvider, UserService userService, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthenticationRequestDto requestDto) {
        try {
            String username = requestDto.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, requestDto.getPassword()));
            User user = userService.getByUsername(username);

            if (user == null) {
                throw new UsernameNotFoundException("User with username: " + username + " not found");
            }

            String accessToken = jwtTokenProvider.createAccessToken(username, user.getRoles());
            String refreshToken = jwtTokenProvider.createRefreshToken(username);

            Map<String, String> response = new HashMap<>();
            response.put("username", username);
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    @PostMapping("register/user")
    public ResponseEntity<String> registerUser(@RequestBody AuthenticationRequestDto requestDto) {
        if (userService.getByUsername(requestDto.getUsername()) != null) {
            throw new JWTAuthenticationException("username is busy");
        }

        Set<Role> newUserRoles = new HashSet<>();
        User newUser = new User(requestDto.getUsername(), requestDto.getPassword(), newUserRoles);

        userService.registerUser(newUser);

        return ResponseEntity.ok(newUser.getUsername());
    }

    @PostMapping("register/admin")
    public ResponseEntity<String> registerAdmin(@RequestBody AuthenticationRequestDto requestDto) {
        if (userService.getByUsername(requestDto.getUsername()) != null) {
            throw new JWTAuthenticationException("username is busy");
        }

        Set<Role> newUserRoles = new HashSet<>();
        newUserRoles.add(Role.ROLE_ADMIN);
        User newUser = new User(requestDto.getUsername(), requestDto.getPassword(), newUserRoles);

        userService.registerUser(newUser);

        return ResponseEntity.ok(newUser.getUsername());
    }

    @PostMapping("refresh")
    public ResponseEntity<String> refresh(@RequestParam UUID refreshToken) {
        User user = refreshTokenService.getUser(refreshToken.toString());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(jwtTokenProvider.createAccessToken(user.getUsername(), user.getRoles()));
    }

    @PostMapping("logout")
    public void logout(@RequestParam String refreshToken) {
        refreshTokenService.deleteRefreshToken(refreshToken);
    }
}
