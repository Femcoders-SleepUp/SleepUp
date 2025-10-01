package com.SleepUp.SU.user.admin;

import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.role.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAdminController userAdminController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAdminServiceImpl userAdminServiceImpl;

    private CustomUserDetails customUserDetails;

    private static final String adminPath = "/api/users/admin";

    @BeforeEach
    void setUp(){
        User savedUser = userRepository.findByUsername("Admin1")
                .orElseThrow(() -> new RuntimeException("User2 not found"));

       customUserDetails = new CustomUserDetails(savedUser);
    }

    @Nested
    class CreateUserTest {

        @Test
        void createUser_adminRole_shouldCreateUser() throws Exception {
            UserRequestAdmin request = new UserRequestAdmin(
                    "newUser",
                    "New User",
                    "newuser@test.com",
                    "password123",
                    Role.USER
            );

            UserResponse response = new UserResponse(
                    10L,
                    "newUser",
                    "New User",
                    "newuser@test.com",
                    Role.USER
            );

            mockMvc.perform(post(adminPath)
                            .with(user(customUserDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("newUser"))
                    .andExpect(jsonPath("$.email").value("newuser@test.com"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        void createUser_notAdminRole_shouldReturnForbidden() throws Exception {
            UserRequestAdmin request = new UserRequestAdmin(
                    "newUser",
                    "New User",
                    "newuser@test.com",
                    "password123",
                    Role.USER
            );

            mockMvc.perform(post(adminPath)
                            .with(user("user").roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class UpdateUserTest {
        @Test
        void updateUser_adminRole_shouldUpdateUser() throws  Exception {
            UserRequestAdmin request = new UserRequestAdmin(
                    "updatedUser",
                    "Updated Name",
                    "updated@test.com",
                    "newPassword",
                    Role.ADMIN
            );

            UserResponse response = new UserResponse(
                    1L,
                    "updatedUser",
                    "Updated Name",
                    "updated@test.com",
                    Role.ADMIN
            );

            mockMvc.perform(put(adminPath + "/" + 1)
                            .with(user(customUserDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("updatedUser"))
                    .andExpect(jsonPath("$.email").value("updated@test.com"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        void updateUser_notAdminRole_shouldReturnForbidden() throws Exception {
            UserRequestAdmin request = new UserRequestAdmin(
                    "updatedUser",
                    "Updated Name",
                    "updated@test.com",
                    "newPassword",
                    Role.ADMIN
            );

            mockMvc.perform(put(adminPath + "/" + 1)
                            .with(user("user").roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

   @Nested
   class DeleteUserTest {

       @Test
       void deleteUser_adminRole_shouldDeleteUser() throws Exception {
           mockMvc.perform(delete(adminPath + "/{id}" , 99L)
                   .with(user(customUserDetails))
                   .accept(MediaType.APPLICATION_JSON))
                   .andExpect(status().isNoContent());
       }

       @Test
       void deleteUser_notAdminRole_shouldReturnForbidden() throws Exception {
           mockMvc.perform(delete(adminPath + "/" + 1)
                           .with(user("user").roles("USER"))
                           .accept(MediaType.APPLICATION_JSON))
                   .andExpect(status().isForbidden());
       }
   }
}
