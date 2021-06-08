package ru.itmo.apiserver.user.api.v1;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.apiserver.user.UserRole;

@RestController
public class UserController {

    @GetMapping("/endpoint0")
    public HttpStatus endpoint0(@RequestHeader("X-ROLE") UserRole role) {
        return HttpStatus.OK;
    }

    @GetMapping("/endpoint1")
    public HttpStatus endpoint1(@RequestHeader("X-ROLE") UserRole role) {
        if(UserRole.ADMIN == role) {
            return HttpStatus.OK;
        } else {
            return HttpStatus.FORBIDDEN;
        }
    }

    @GetMapping("/endpoint2")
    public HttpStatus endpoint2(@RequestHeader("X-ROLE") UserRole role) {
        return HttpStatus.FORBIDDEN;
    }
}
