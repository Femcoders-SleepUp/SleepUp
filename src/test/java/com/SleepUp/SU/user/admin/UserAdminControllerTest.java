package com.SleepUp.SU.user.admin;

import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.role.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserAdminService userAdminService;

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

            when(userAdminService.createUser(any(UserRequestAdmin.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/users")
                            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
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

            mockMvc.perform(post("/api/users")
                            .with(SecurityMockMvcRequestPostProcessors.user("user").roles("USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

}
