package com.SleepUp.SU.user.user;

import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.role.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.SleepUp.SU.user.CustomUserDetails;
import com.SleepUp.SU.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUserController userUserController;

    @MockBean
    private UserUserService userUserService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        User savedUser = userRepository.findByUsername("TestUser").get();
        customUserDetails = new CustomUserDetails(savedUser);
    }

    @Nested
    class getLoggedUser{

        @Test
        void when_authenticated_then_return_logged_user() throws Exception {

            UserResponse response = new UserResponse(99L, "testUser", "Test Name", "test@email.com", Role.USER);

            when(userUserService.getLoggedUser(anyLong())).thenReturn(response);

            mockMvc.perform(get("/api/users/my-user")
                            .with(user(customUserDetails))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("testUser"))
                    .andExpect(jsonPath("$.name").value("Test Name"))
                    .andExpect(jsonPath("$.email").value("test@email.com"));
        }

        @Test
        void when_not_authenticated_then_return_unauthorized() throws Exception {
            mockMvc.perform(get("/api/users/my-user")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class updateLoggedUser{

        @Test
        void when_authenticated_then_update_logged_user() throws Exception {
            UserRequest request = new UserRequest("updatedUser", "updateName", "updated@email.com", "newPassword");
            UserResponse response = new UserResponse(99L, "updatedUser", "updateName", "updated@email.com", Role.USER);

            when(userUserService.updateLoggedUser(any(UserRequest.class), anyLong())).thenReturn(response);

            mockMvc.perform(
                            put("/api/users/my-user")
                                    .with(user(customUserDetails))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                                    .accept(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("updatedUser"))
                    .andExpect(jsonPath("$.email").value("updated@email.com"))
                    .andExpect(jsonPath("$.name").value("updateName"));
        }

        @Test
        void when_not_authenticated_then_update_fails_with_unauthorized() throws Exception {
            String jsonRequest = """
        {
            "username": "updatedUser",
            "email": "updated@email.com",
            "password": "newPassword"
        }
        """;

            mockMvc.perform(
                            put("/api/users/my-user")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonRequest)
                                    .accept(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void when_service_throws_exception_then_return_bad_request() throws Exception {
            UserRequest request = new UserRequest("updatedUser", "updateName",  "updated@email.com", "newPassword");

            when(userUserService.updateLoggedUser(request, 99L))
                    .thenThrow(new IllegalArgumentException("Invalid update"));

            String jsonRequest = """
        {
            "username": "updatedUser",
            "email": "updated@email.com",
            "password": "newPassword"
        }
        """;

            mockMvc.perform(
                            put("/api/users/my-user")
                                    .with(user(customUserDetails))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonRequest)
                                    .accept(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class deleteLoggedUser {

        @Test
        void when_authenticated_then_delete_logged_user() throws Exception {
            mockMvc.perform(delete("/api/users/my-user")
                            .with(user(customUserDetails))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Account deleted!!"));
        }

        @Test
        void when_not_authenticated_then_delete_fails_with_unauthorized() throws Exception {
            mockMvc.perform(delete("/api/users/my-user")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
