package com.SleepUp.SU.user.admin;

import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.role.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserAdminControllerTest {

    private static final String BASE_PATH = "/users/admin";
    private static final String USER_PATH_ID = "/users/admin/{id}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private CustomUserDetails adminCustomUserDetails;
    private CustomUserDetails userCustomUserDetails;

    private UserRequestAdmin userRequestAdmin;
    private UserRequestAdmin updateUserRequestAdmin;

    @BeforeEach
    void setUp() {
        User savedAdmin = userRepository.findByUsername("Admin1")
                .orElseThrow(() -> new RuntimeException("Admin1 not found"));
        adminCustomUserDetails = new CustomUserDetails(savedAdmin);

        User savedUser = userRepository.findByUsername("User1")
                .orElseThrow(() -> new RuntimeException("User1 not found"));
        userCustomUserDetails = new CustomUserDetails(savedUser);

        userRequestAdmin = UserRequestAdmin.builder()
                .username("newUser")
                .name("New User")
                .email("newuser@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build();

        updateUserRequestAdmin = UserRequestAdmin.builder()
                .username("updatedUser")
                .name("Updated Name")
                .email("updated@test.com")
                .password(passwordEncoder.encode("newPassword"))
                .role(Role.ADMIN)
                .build();
    }

    @Nested
    class CreateUserTest {

        @Test
        void createUser_whenAdminRole_shouldReturnCreated() throws Exception {
            mockMvc.perform(post(BASE_PATH)
                            .with(user(adminCustomUserDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRequestAdmin)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("newUser"))
                    .andExpect(jsonPath("$.email").value("newuser@test.com"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        void createUser_whenNotAdminRole_shouldReturnForbidden() throws Exception {
            mockMvc.perform(post(BASE_PATH)
                            .with(user(userCustomUserDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRequestAdmin)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void createUser_whenNoAuth_shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRequestAdmin)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class UpdateUserTest {

        @Test
        void updateUser_whenAdminRole_shouldReturnOk() throws Exception {
            mockMvc.perform(put(USER_PATH_ID, 1L)
                            .with(user(adminCustomUserDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserRequestAdmin)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("updatedUser"))
                    .andExpect(jsonPath("$.email").value("updated@test.com"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        void updateUser_whenNotAdminRole_shouldReturnForbidden() throws Exception {
            mockMvc.perform(put(USER_PATH_ID, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserRequestAdmin))
                            .with(user(userCustomUserDetails)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void updateUser_whenNoAuth_shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(put(USER_PATH_ID, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserRequestAdmin)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class DeleteUserTest {

        @Test
        void deleteUser_whenAdminRole_shouldReturnNoContent() throws Exception {
            mockMvc.perform(delete(USER_PATH_ID, 99L)
                            .with(user(adminCustomUserDetails))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }

        @Test
        void deleteUser_whenNotAdminRole_shouldReturnForbidden() throws Exception {
            mockMvc.perform(delete(USER_PATH_ID, 1L)
                            .with(user(userCustomUserDetails))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        void deleteUser_whenNoAuth_shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(delete(USER_PATH_ID, 1L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
