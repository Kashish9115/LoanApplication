package com.example.loanApplication.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtUtils.validateToken(token)) {

            String tokenType = jwtUtils.getClaimFromToken(
                    token,
                    "type"
            );

            /*
             * Sirf final ACCESS token wale user ko hi
             * Spring Security context mein authenticated
             * set karenge.
             *
             * PRE_AUTH token wale user ko authenticate nahi
             * karenge, taaki wo 2FA bypass na kar sake.
             */
            if ("ACCESS".equals(tokenType)) {

                String email = jwtUtils.getEmailFromToken(token);

                String role = jwtUtils.getClaimFromToken(
                        token,
                        "role"
                );

                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority(
                                "ROLE_" + role.toUpperCase()
                        );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.singletonList(authority)
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    /*
     * Helper method:
     * 1. First checks HTTP-Only Cookie
     * 2. Falls back to Authorization Header
     *    (useful for Postman)
     */
    private String extractToken(HttpServletRequest request) {

        // 1. Check HttpOnly Cookie
        if (request.getCookies() != null) {

            for (Cookie cookie : request.getCookies()) {

                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 2. Fallback to Authorization Header
        //    (Bearer token for Postman)
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null
                && authHeader.startsWith("Bearer ")) {

            return authHeader.substring(7);
        }

        return null;
    }
}
