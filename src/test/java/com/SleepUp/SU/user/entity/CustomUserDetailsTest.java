package com.SleepUp.SU.user.entity;

import com.SleepUp.SU.user.role.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    void getAuthorities_withRole_shouldReturnAuthorityList() {
        Role role = Role.ADMIN;

        User user = new User();
        user.setRole(role);

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();

        assertThat(authorities).hasSize(1);
        GrantedAuthority authority = authorities.iterator().next();
        assertThat(authority).isInstanceOf(SimpleGrantedAuthority.class);
        assertThat(authority.getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void getAuthorities_withNullRole_shouldReturnEmptyList() {
        User user = new User();
        user.setRole(null);

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();

        assertThat(authorities).isEmpty();
    }
}