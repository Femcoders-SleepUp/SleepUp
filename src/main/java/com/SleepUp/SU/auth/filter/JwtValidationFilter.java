package com.SleepUp.SU.auth.filter;

import com.SleepUp.SU.auth.AuthServiceHelper;
import com.SleepUp.SU.auth.SimpleGrantedAuthorityJsonCreator;
import com.SleepUp.SU.utils.ApiMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static com.SleepUp.SU.auth.TokenJwtConfig.*;

public class JwtValidationFilter extends BasicAuthenticationFilter {

    private final AuthServiceHelper authServiceHelper;

    public JwtValidationFilter(AuthenticationManager authenticationManager, AuthServiceHelper authServiceHelper) {
        super(authenticationManager);
        this.authServiceHelper = authServiceHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader(headerAuthorization);

        if (header == null || !header.startsWith(prefixToken)){
            chain.doFilter(request,response);
            return;
        }

        String token = header.replace(prefixToken, "");
        try {
            Claims claims = authServiceHelper.validateAccessToken(token);

            String username = claims.getSubject();
            Object authoritiesClaims = claims.get("authorities");
            Collection<? extends GrantedAuthority> authorities = Arrays.asList(new ObjectMapper()
                    .addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityJsonCreator.class)
                    .readValue(authoritiesClaims.toString().getBytes(), SimpleGrantedAuthority[].class));

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            chain.doFilter(request,response);
        } catch (JwtException e) {
            ApiMessageDto body =new ApiMessageDto("Error: " + e.getMessage() +
                "\nMessage: Invalid token");

            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            response.setContentType(contentType);
        }
    }
}
