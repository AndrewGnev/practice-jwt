package ru.itmo.authjwtserver.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.itmo.authjwtserver.user.model.Role;
import ru.itmo.authjwtserver.user.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, BCryptPasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        Set<Role> roles = new HashSet<Role>();
        roles.add(Role.USER);
        user.setRoles(roles);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return repository.save(user);
    }

    public List<User> getUsers() {
        return repository.findAll();
    }

    public User getByUsername(String username) {
        return repository.findByUsername(username);
    }

    public User getById(Long id) {
        return repository.findById(id).orElse(null);
    }




}
