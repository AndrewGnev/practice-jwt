package ru.itmo.authjwtserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@SpringBootApplication
@EnableGlobalMethodSecurity(jsr250Enabled = true)
public class AuthJwtServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthJwtServerApplication.class, args);
    }

}
