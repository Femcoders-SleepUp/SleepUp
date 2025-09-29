package com.SleepUp.SU.security.config;

import com.SleepUp.SU.auth.TokenBlacklistService;
import com.SleepUp.SU.security.CustomAccessDeniedHandler;
import com.SleepUp.SU.security.RestAuthenticationEntryPoint;
import com.SleepUp.SU.security.jwt.JwtAuthFilter;
import com.SleepUp.SU.security.jwt.JwtService;
import com.SleepUp.SU.user.admin.UserAdminServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtService jwtService, UserAdminServiceImpl userService, TokenBlacklistService tokenBlacklistService) {
        return new JwtAuthFilter(restAuthenticationEntryPoint, jwtService, userService, tokenBlacklistService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(ApiPrefixHelper.prefixPaths("/swagger-ui/**", "/v3/api-docs/**")).permitAll()

                        .requestMatchers(HttpMethod.GET, "/health").permitAll()

                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, ("/auth/register")).permitAll()
                        .requestMatchers(HttpMethod.POST, ("/auth/logout")).authenticated()
                        .requestMatchers(HttpMethod.POST, ("/auth/refresh")).authenticated()

                        .requestMatchers(HttpMethod.POST, ApiPrefixHelper.prefixPaths("/auth/login")).permitAll()
                        .requestMatchers(HttpMethod.POST, ApiPrefixHelper.prefixPaths("/auth/register")).permitAll()
                        .requestMatchers(HttpMethod.POST, ApiPrefixHelper.prefixPaths("/auth/logout")).authenticated()
                        .requestMatchers(HttpMethod.POST, ApiPrefixHelper.prefixPaths("/auth/refresh")).authenticated()

                        .requestMatchers(HttpMethod.GET, ApiPrefixHelper.prefixPaths("/users/me")).authenticated()
                        .requestMatchers(HttpMethod.PUT, ApiPrefixHelper.prefixPaths("/users/me")).authenticated()
                        .requestMatchers(HttpMethod.DELETE, ApiPrefixHelper.prefixPaths("/users/me")).authenticated()

                        .requestMatchers(HttpMethod.GET, ApiPrefixHelper.prefixPaths("/users/admin/**")).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, ApiPrefixHelper.prefixPaths("/users/admin/**")).hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, ApiPrefixHelper.prefixPaths("/reservations/admin")).hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, ApiPrefixHelper.prefixPaths("/accommodations")).permitAll()
                        .requestMatchers(HttpMethod.GET, "/accommodations").permitAll()
                        .requestMatchers(HttpMethod.GET, ApiPrefixHelper.prefixPaths("/accommodations/filter**")).permitAll()
                        .requestMatchers(HttpMethod.GET, "/accommodations/filter**").permitAll()

                        .requestMatchers(HttpMethod.GET, ApiPrefixHelper.prefixPaths("/accommodations/me")).hasRole("USER")
                        .requestMatchers(HttpMethod.POST, ApiPrefixHelper.prefixPaths("/accommodations")).hasRole("USER")

                        .requestMatchers(HttpMethod.PUT, ApiPrefixHelper.prefixPaths("/accommodations/**")).authenticated()
                        .requestMatchers(HttpMethod.DELETE, ApiPrefixHelper.prefixPaths("/accommodations/**")).authenticated()

                        .requestMatchers(HttpMethod.POST, ApiPrefixHelper.prefixPaths("/accommodations/**")).hasRole("USER")
                        .requestMatchers(HttpMethod.GET, ApiPrefixHelper.prefixPaths("/accommodations/**")).hasRole("USER")

                        .requestMatchers(HttpMethod.PATCH, ApiPrefixHelper.prefixPaths("/accommodations/**")).authenticated()

                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
