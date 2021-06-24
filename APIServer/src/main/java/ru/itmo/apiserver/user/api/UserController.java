package ru.itmo.apiserver.user.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/endpoint0")
    public void endpoint0() {
    }

    @GetMapping("/endpoint1")
    @RolesAllowed("ROLE_ADMIN")
    public void endpoint1() {
    }

    @GetMapping("/endpoint2")
    public void endpoint2() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
}
