package ru.itmo.authjwtserver.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.itmo.authjwtserver.user.model.Role;
import ru.itmo.authjwtserver.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class JWTUserFactory {

    public JWTUserFactory() {
    }

    public static JWTUser create(User user) {
        return new JWTUser(user.getId(), user.getUsername(), user.getPassword(),
                mapRolesToGrantedAuthorities(new ArrayList<>(user.getRoles())));
    }

    private static List<GrantedAuthority> mapRolesToGrantedAuthorities(List<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name())).collect(Collectors.toList());
    }
}
