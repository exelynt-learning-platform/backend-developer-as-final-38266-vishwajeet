
package com.rms.Security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        // No Authorization header
        // Let Spring Security handle authentication/authorization.
        if (authHeader == null || authHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization header exists but is not Bearer
        if (!authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(
                    response,
                    "Invalid Authorization header");
            return;
        }

        String jwt = authHeader.substring(7).trim();

        // Empty Bearer token
        if (jwt.isEmpty()) {
            sendUnauthorizedResponse(
                    response,
                    "JWT token is missing");
            return;
        }

        try {

            String username =
                    jwtService.extractUsername(jwt);

            if (username == null || username.isBlank()) {
                sendUnauthorizedResponse(
                        response,
                        "Invalid JWT token");
                return;
            }

            /*
             * Only authenticate if there is no existing
             * authentication in the SecurityContext.
             */
            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(username);

                if (!jwtService.isTokenValid(
                        jwt,
                        userDetails)) {

                    sendUnauthorizedResponse(
                            response,
                            "Invalid or expired JWT token");
                    return;
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }

            // JWT is valid
            filterChain.doFilter(request, response);

        } catch (Exception e) {

            /*
             * JWT processing failed.
             *
             * Do NOT continue the filter chain.
             * Return HTTP 401 Unauthorized.
             */
            SecurityContextHolder
                    .clearContext();

            sendUnauthorizedResponse(
                    response,
                    "Invalid or expired JWT token");
        }
    }

    private void sendUnauthorizedResponse(
            HttpServletResponse response,
            String message)
            throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED);

        response.setContentType(
                "application/json");

        response.getWriter().write(
                "{\"error\":\"" + message + "\"}"
        );
    }
}
