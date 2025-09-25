package com.SleepUp.SU.user.admin;

import com.SleepUp.SU.user.CustomUserDetails;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
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
    private  UserAdminService userAdminService;

   private CustomUserDetails customUserDetails;

   @BeforeEach
   void sepUp(){
        User savedUser = userRepository.findByUsername("Admin1")
                .orElseThrow(() -> new RuntimeException("User2 not found"));

       customUserDetails = new CustomUserDetails(savedUser);
    }


    @Nested
    class CreateUserTest {

        @Test
        void when_adminRole_then_createUser() throws Exception {
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


            mockMvc.perform(post("/api/users/admin")
                            .with(user(customUserDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("newUser"))
                    .andExpect(jsonPath("$.email").value("newuser@test.com"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        void when_notAdminRole_then_forbidden() throws Exception {
            UserRequestAdmin request = new UserRequestAdmin(
                    "newUser",
                    "New User",
                    "newuser@test.com",
                    "password123",
                    Role.USER
            );

            mockMvc.perform(post("/api/users/admin")
                            .with(user("user").roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class UpdateUserTest {
        @Test
        void when_adminRole_then_updateUser() throws  Exception {
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


            mockMvc.perform(put("/api/users/admin/1")
                            .with(user(customUserDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("updatedUser"))
                    .andExpect(jsonPath("$.email").value("updated@test.com"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        void when_notAdminRole_then_forbidden() throws Exception {
            UserRequestAdmin request = new UserRequestAdmin(
                    "updatedUser",
                    "Updated Name",
                    "updated@test.com",
                    "newPassword",
                    Role.ADMIN
            );

            mockMvc.perform(put("/api/users/admin/1")
                            .with(user("user").roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

   @Nested
   class DeleteUserTest {

       @Test
       void when_adminRole_then_deleteUser() throws Exception {
           mockMvc.perform(delete("/api/users/admin/{id}",99L)
                   .with(user(customUserDetails))
                   .accept(MediaType.APPLICATION_JSON))
                   .andExpect(status().isNoContent());
       }
       @Test
       void when_notAdminRole_then_forbidden() throws Exception {
           mockMvc.perform(delete("/api/users/admin/1")
                           .with(user("user").roles("USER"))
                           .accept(MediaType.APPLICATION_JSON))
                   .andExpect(status().isForbidden());
       }
   }
}
