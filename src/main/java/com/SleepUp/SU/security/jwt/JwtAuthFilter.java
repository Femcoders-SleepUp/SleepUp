package com.SleepUp.SU.security.jwt;

import com.SleepUp.SU.auth.TokenBlacklistService;
import com.SleepUp.SU.security.RestAuthenticationEntryPoint;
import com.SleepUp.SU.user.admin.UserAdminServiceImpl;
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
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/auth/login",
            "/auth/register"
    );

    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final JwtService jwtService;
    private final UserAdminServiceImpl userService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(BEARER_PREFIX.length());

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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Skip the filter for excluded paths with POST method
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.contains(path) && "POST".equalsIgnoreCase(request.getMethod());
    }
}
