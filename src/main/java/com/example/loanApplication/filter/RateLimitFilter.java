
package com.example.loanApplication.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000L;

    private static final int GENERAL_LIMIT = 5;
    private static final int LOGIN_LIMIT = 5;
    private static final int REGISTER_LIMIT = 3;
    private static final int OTP_LIMIT = 5;

    private final Map<String, WindowCounter> counters =
            new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String clientIp = request.getRemoteAddr();

        int limit = getLimit(path);
        String category = getCategory(path);
        String key = clientIp + ":" + category;

        long now = System.currentTimeMillis();

        WindowCounter counter = counters.compute(key, (k, existing) -> {
            if (existing == null
                    || now - existing.windowStart >= WINDOW_MILLIS) {
                return new WindowCounter(now, 1);
            }

            existing.count++;
            return existing;
        });

        response.setHeader(
                "X-RateLimit-Limit",
                String.valueOf(limit)
        );

        response.setHeader(
                "X-RateLimit-Remaining",
                String.valueOf(Math.max(0, limit - counter.count))
        );

        if (counter.count > limit) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String timestamp = Instant.now().toString();

            String json = """
                    {
                      "success": false,
                      "message": "Too many requests. Please try again later.",
                      "status": 429,
                      "timestamp": "%s"
                    }
                    """.formatted(timestamp);

            response.getWriter().write(json);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private int getLimit(String path) {
        String normalizedPath = path.toLowerCase();

        if (normalizedPath.contains("login")) {
            return LOGIN_LIMIT;
        }

        if (normalizedPath.contains("register")) {
            return REGISTER_LIMIT;
        }

        if (normalizedPath.contains("otp")) {
            return OTP_LIMIT;
        }

        return GENERAL_LIMIT;
    }

    private String getCategory(String path) {
        String normalizedPath = path.toLowerCase();

        if (normalizedPath.contains("login")) {
            return "LOGIN";
        }

        if (normalizedPath.contains("register")) {
            return "REGISTER";
        }

        if (normalizedPath.contains("otp")) {
            return "OTP";
        }

        return "GENERAL";
    }

    private static class WindowCounter {

        private final long windowStart;
        private int count;

        private WindowCounter(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
