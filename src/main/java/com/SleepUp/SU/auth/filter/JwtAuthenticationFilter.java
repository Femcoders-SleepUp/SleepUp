package com.SleepUp.SU.auth.filter;

import com.SleepUp.SU.auth.AuthServiceHelper;
import com.SleepUp.SU.auth.dto.AuthResponse;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.utils.ApiMessageDto;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.SleepUp.SU.auth.TokenJwtConfig.*;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;
    private AuthServiceHelper authServiceHelper;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, AuthServiceHelper authServiceHelper) {
        this.authenticationManager = authenticationManager;
        this.authServiceHelper = authServiceHelper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = null;
        String password = null;
        try {
            User user = new ObjectMapper().readValue(request.getInputStream(), User.class);
            username = user.getUsername();
            password = user.getPassword();
        } catch (StreamReadException e) {
            e.printStackTrace();
        } catch (DatabindException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        return this.authenticationManager.authenticate(authenticationToken);

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authResult.getPrincipal();
        String username = user.getUsername();
        Collection<? extends GrantedAuthority> roles = authResult.getAuthorities();

        Claims claims = Jwts.claims()
                .add("authorities", new ObjectMapper().writeValueAsString(roles))
                .add("username", username)
                .build();

        String accessToken = authServiceHelper.generateAccessToken(username, claims);

        String refreshToken = authServiceHelper.generateRefreshToken(username);

        response.addHeader(headerAuthorization, prefixToken + accessToken);

        AuthResponse body = new AuthResponse(
                String.format("Session started successfully. Hello " + username),
                accessToken,
                username,
                refreshToken
        );

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setContentType(contentType);
        response.setStatus(200);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        ApiMessageDto body = new ApiMessageDto("Message: Authentication failed.\n" +
                "Error: " + failed.getMessage());

        response.getWriter().write(new ObjectMapper().writeValueAsString((body)));
        response.setContentType(contentType);
        response.setStatus(401);
    }

}
