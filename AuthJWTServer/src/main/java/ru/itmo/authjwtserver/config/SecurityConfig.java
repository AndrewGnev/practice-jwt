package ru.itmo.authjwtserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import ru.itmo.authjwtserver.security.JWTAuthenticationProvider;
import ru.itmo.authjwtserver.security.JWTConfigurer;
import ru.itmo.authjwtserver.user.UserService;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JWTConfigurer jwtConfigurer;
    private final UserService userService;
    private final JWTAuthenticationProvider authProvider;

    @Autowired
    public SecurityConfig(JWTConfigurer jwtConfigurer, JWTAuthenticationProvider authProvider, UserService userService) {
        this.jwtConfigurer = jwtConfigurer;
        this.authProvider = authProvider;
        this.userService = userService;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
        auth.authenticationProvider(authProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .and()
                .apply(jwtConfigurer);
    }
}

