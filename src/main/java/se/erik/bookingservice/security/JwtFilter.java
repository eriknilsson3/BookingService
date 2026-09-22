package se.erik.bookingservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    JwtFilter(JwtService j){
        this.jwt = j; }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        String authorizationHeader = req.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        String token = authorizationHeader.substring(7);

        if (jwt.isTokenValid(token)) {

            Long customerId = Long.parseLong(jwt.extractUsername(token));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            customerId,
                            null,
                            Collections.emptyList()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("=== JWT VALID ===");
            System.out.println("USER = " + customerId);
            System.out.println("AUTHENTICATED = " +
                    SecurityContextHolder.getContext()
                            .getAuthentication()
                            .isAuthenticated());
        }

        System.out.println("=== BEFORE CONTROLLER CHAIN ===");

        chain.doFilter(req, res);

        System.out.println("=== AFTER CONTROLLER CHAIN ===");
    }

}


