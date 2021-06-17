package ru.itmo.authjwtserver.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.itmo.authjwtserver.user.UserService;
import ru.itmo.authjwtserver.user.model.User;

@Service
public class JWTUserDetailsService implements UserDetailsService {
    private final UserService service;

    public JWTUserDetailsService(UserService service) {
        this.service = service;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = service.getByUsername(username);

        if(user == null) throw new UsernameNotFoundException("user " + username + " not found");

        return JWTUserFactory.create(user);
    }
}
