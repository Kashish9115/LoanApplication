package com.example.loanApplication.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Protects officer actions with a configurable shared API key until this module is wired to the main JWT auth system. */
@Component
public class OfficerApiKeyFilter extends OncePerRequestFilter {

    private final String officerApiKey;

    public OfficerApiKeyFilter(@Value("${app.security.officer-api-key}") String officerApiKey) {
        this.officerApiKey = officerApiKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/kyc/officer/") || path.startsWith("/api/support/officer/")) {
            return false;
        }
        // Document files and destructive operations must not be exposed without officer credentials.
        if (path.matches("/api/kyc/documents/\\d+/file") ||
                ("DELETE".equalsIgnoreCase(request.getMethod()) && path.startsWith("/api/kyc/documents/")) ||
                ("DELETE".equalsIgnoreCase(request.getMethod()) && path.startsWith("/api/support/tickets/"))) {
            return false;
        }
        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String providedKey = request.getHeader("X-Officer-Key");
        if (providedKey == null || !MessageDigest.isEqual(
                officerApiKey.getBytes(StandardCharsets.UTF_8), providedKey.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"A valid X-Officer-Key header is required for officer actions.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
