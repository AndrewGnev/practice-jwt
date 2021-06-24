package ru.itmo.apiserver.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JWTTokenFilter extends GenericFilterBean {

    private final JWTTokenProvider jwtTokenProvider;

    public JWTTokenFilter(JWTTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
        if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
            doFilter((HttpServletRequest) req, (HttpServletResponse) res, filterChain);
        } else {
            filterChain.doFilter(req, res);
        }
    }

    private void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = jwtTokenProvider.resolveAccessToken(req);
            if (token != null && jwtTokenProvider.validateAccessToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);

                if (auth != null) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

            filterChain.doFilter(req, res);
        } catch (JWTAuthenticationException e) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
