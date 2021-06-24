package ru.itmo.authjwtserver.user.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.authjwtserver.security.JWTAuthenticationException;
import ru.itmo.authjwtserver.security.JWTTokenProvider;
import ru.itmo.authjwtserver.security.refresh.RefreshToken;
import ru.itmo.authjwtserver.security.refresh.RefreshTokenService;
import ru.itmo.authjwtserver.user.UserService;
import ru.itmo.authjwtserver.user.model.Role;
import ru.itmo.authjwtserver.user.model.User;

import javax.annotation.security.RolesAllowed;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    public ResponseEntity<AuthenticationResponseDto> login(@RequestBody AuthenticationRequestDto requestDto) {
        try {
            String username = requestDto.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, requestDto.getPassword()));

            User user = userService.getByUsernameStrict(username);

            String accessToken = jwtTokenProvider.createAccessToken(username, user.getRoles());
            String refreshToken = jwtTokenProvider.createRefreshToken(username);

            return ResponseEntity.ok(new AuthenticationResponseDto(accessToken, refreshToken));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    @PostMapping("register/user")
    public ResponseEntity<String> registerUser(@RequestBody AuthenticationRequestDto requestDto) {
        if (userService.getByUsername(requestDto.getUsername()).isPresent()) {
            throw new JWTAuthenticationException("username is busy");
        }

        Set<Role> newUserRoles = new HashSet<>();
        User newUser = new User(requestDto.getUsername(), requestDto.getPassword(), newUserRoles);

        userService.registerUser(newUser);

        return ResponseEntity.ok(newUser.getUsername());
    }

    @RolesAllowed("ADMIN")
    @PostMapping("register/admin")
    public ResponseEntity<String> registerAdmin(@RequestBody AuthenticationRequestDto requestDto) {
        if (userService.getByUsername(requestDto.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        Set<Role> newUserRoles = new HashSet<>();
        newUserRoles.add(Role.ROLE_ADMIN);
        User newUser = new User(requestDto.getUsername(), requestDto.getPassword(), newUserRoles);

        userService.registerUser(newUser);

        return ResponseEntity.ok(newUser.getUsername());
    }

    @PostMapping("refresh")
    public ResponseEntity<AuthenticationResponseDto> refresh(@RequestParam UUID refreshToken) {
        RefreshToken token = refreshTokenService.getRefreshToken(refreshToken.toString());

        if (token == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (token.getValidUntil().isBefore(Instant.now())) {
            refreshTokenService.deleteRefreshToken(token.getUser().getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User user = token.getUser();
        return ResponseEntity.ok(new AuthenticationResponseDto(
                jwtTokenProvider.createAccessToken(user.getUsername(), user.getRoles()),
                refreshTokenService.generateRefreshToken(user.getUsername()).getToken()
        ));
    }

    @PostMapping("logout")
    @PreAuthorize("isAuthenticated()")
    public void logout(@AuthenticationPrincipal String username) {
        refreshTokenService.deleteRefreshToken(username);
    }
}
