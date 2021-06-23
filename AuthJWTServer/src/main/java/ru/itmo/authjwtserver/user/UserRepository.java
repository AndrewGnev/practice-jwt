package ru.itmo.authjwtserver.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.authjwtserver.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername (String username);
}
