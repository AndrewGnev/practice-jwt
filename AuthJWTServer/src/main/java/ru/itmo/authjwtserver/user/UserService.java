package ru.itmo.authjwtserver.user;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.itmo.authjwtserver.user.model.Role;
import ru.itmo.authjwtserver.user.model.User;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, BCryptPasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        user.getRoles().add(Role.ROLE_USER);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return repository.save(user);
    }

    public List<User> getUsers() {
        return repository.findAll();
    }

    public User getByUsernameStrict(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("user " + username + " not found"));
    }

    public Optional<User> getByUsername(String username) {
        return repository.findByUsername(username);
    }

    public User getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return getByUsernameStrict(username);
    }
}
