package ru.itmo.apiserver.user.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

@RestController
public class UserController {

    @GetMapping("/endpoint0")
    public HttpStatus endpoint0() {
        return HttpStatus.OK;
    }

    @GetMapping("/endpoint1")
    @RolesAllowed("ROLE_ADMIN")
    public HttpStatus endpoint1() {
        return HttpStatus.OK;
    }

    @GetMapping("/endpoint2")
    public HttpStatus endpoint2() {
        return HttpStatus.FORBIDDEN;
    }
}
