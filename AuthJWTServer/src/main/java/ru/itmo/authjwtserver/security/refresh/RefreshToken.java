package ru.itmo.authjwtserver.security.refresh;

import ru.itmo.authjwtserver.user.model.User;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private User user;
    private String token;
    private Instant createdDate;

    public RefreshToken() {
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
