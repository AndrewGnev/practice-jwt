package ru.itmo.authjwtserver.user.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.authjwtserver.security.JWTUser;

@RestController
@RequestMapping("/lol/")
public class Controller {

    @GetMapping("kek")
    public JWTUser kek(@AuthenticationPrincipal JWTUser jwtUser) {
        return jwtUser;
    }
}
