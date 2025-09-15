package com.SleepUp.SU.user.admin;

import com.SleepUp.SU.security.RestAuthenticationEntryPoint;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.role.Role;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings({"deprecation", "unused"})
@WebMvcTest(UserAdminController.class)
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAdminService userService;

    @MockBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Nested
    class ListAllUsers {

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        void should_returnAllUsers() throws Exception {
            List<UserResponse> users = List.of(
                    new UserResponse(1L, "userOne", "User One", "user1@test.com", Role.USER),
                    new UserResponse(2L, "userTwo", "User Two", "user2@test.com", Role.ADMIN)
            );

            when(userService.getAllUsers()).thenReturn(users);

            mockMvc.perform(get("/api/users")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2))
                    .andExpect(jsonPath("$[0].username").value("userOne"))
                    .andExpect(jsonPath("$[1].username").value("userTwo"));
        }
    }

    @Nested
    class GetUserBy {

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        void when_admin_then_returnUser() throws Exception {
            UserResponse user = new UserResponse(1L, "userOne", "User One", "user1@test.com", Role.USER);

            when(userService.getUserById(anyLong())).thenReturn(user);

            mockMvc.perform(get("/api/users/1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("userOne"))
                    .andExpect(jsonPath("$.email").value("user1@test.com"));
        }

        @Test
        @WithMockUser(username = "normalUser", roles = {"USER"})
        void when_notAdmin_then_forbidden() throws Exception {
            mockMvc.perform(get("/api/users/1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }
}