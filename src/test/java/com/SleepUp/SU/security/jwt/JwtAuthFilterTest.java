package com.SleepUp.SU.security.jwt;

import com.SleepUp.SU.auth.TokenBlacklistService;
import com.SleepUp.SU.security.RestAuthenticationEntryPoint;
import com.SleepUp.SU.user.admin.UserAdminServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain filterChain;
    @Mock JwtService jwtService;
    @Mock TokenBlacklistService tokenBlacklistService;
    @Mock UserAdminServiceImpl userService;
    @Mock RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @InjectMocks
    JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_noAuthHeader_callsFilterChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userService, restAuthenticationEntryPoint);
    }

    @Test
    void testDoFilterInternal_invalidAuthHeader_callsFilterChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userService, restAuthenticationEntryPoint);
    }

    @Test
    void testDoFilterInternal_invalidToken_throwsException_callsCommence() throws Exception {
        String token = "token123";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValidToken(token)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(restAuthenticationEntryPoint).commence(eq(request), eq(response), any(AuthenticationCredentialsNotFoundException.class));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void testDoFilterInternal_tokenInBlacklist_throwsException_callsCommence() throws Exception {
        String token = "token123";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValidToken(token)).thenReturn(true);
        when(tokenBlacklistService.isTokenInBlacklist(token)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(restAuthenticationEntryPoint).commence(eq(request), eq(response), any(AuthenticationCredentialsNotFoundException.class));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void testDoFilterInternal_authenticationException_callsCommenceAndClearsContext() throws Exception {
        String token = "token123";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValidToken(token)).thenReturn(true);
        when(tokenBlacklistService.isTokenInBlacklist(token)).thenReturn(false);
        when(jwtService.extractUsername(token)).thenReturn("user");
        when(userService.loadUserByUsername("user")).thenThrow(new AuthenticationCredentialsNotFoundException("fail"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(restAuthenticationEntryPoint).commence(eq(request), eq(response), any(AuthenticationException.class));
        assert SecurityContextHolder.getContext().getAuthentication() == null;
        verify(filterChain, never()).doFilter(any(), any());
    }
}