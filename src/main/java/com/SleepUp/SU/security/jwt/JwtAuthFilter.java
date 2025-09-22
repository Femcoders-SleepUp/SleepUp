package com.SleepUp.SU.security.jwt;

import com.SleepUp.SU.auth.TokenBlacklistService;
import com.SleepUp.SU.security.RestAuthenticationEntryPoint;
import com.SleepUp.SU.user.admin.UserAdminService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final JwtService jwtService;
    private final UserAdminService userService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (shouldSkipFilter(requestURI, request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            if (!jwtService.isValidToken(token) || tokenBlacklistService.isTokenInBlacklist(token)) {
                throw new AuthenticationCredentialsNotFoundException("Invalid JWT token");
            }

            String username = jwtService.extractUsername(token);
            UserDetails userDetails = userService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);

        } catch (AuthenticationException exception) {
            SecurityContextHolder.clearContext();

            restAuthenticationEntryPoint.commence(request, response, exception);
        }
    }


    private boolean shouldSkipFilter(String requestURI, String method) {
        return (requestURI.equals("/api/auth/login") && "POST".equals(method)) ||
                (requestURI.equals("/api/auth/register") && "POST".equals(method));
    }
}

