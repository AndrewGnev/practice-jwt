package ru.itmo.authjwtserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
//import ru.itmo.authjwtserver.security.JWTConfigurer;
import ru.itmo.authjwtserver.security.JWTTokenProvider;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JWTTokenProvider jwtTokenProvider;

    @Autowired
    public SecurityConfig(JWTTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll();
                /*.anyRequest().authenticated()
                .and()
                .apply(new JWTConfigurer(jwtTokenProvider));*/
    }
}

