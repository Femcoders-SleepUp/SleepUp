package com.SleepUp.SU.auth;

import com.SleepUp.SU.auth.dto.*;
import com.SleepUp.SU.user.CustomUserDetails;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.utils.ApiMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthController authController;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private CustomUserDetails principal;

    @BeforeEach
    void setUp() {
        User savedUser = userRepository.findByUsername("TestUser").get();
        System.out.println(savedUser.getName());
        System.out.println(savedUser.getUsername());
        principal = new CustomUserDetails(savedUser);
    }

    @Test
    void when_register_then_return_created_user() throws Exception {
        UserRequest request = new UserRequest("newUser", "New Name", "new@email.com", "password123");
        UserResponse response = new UserResponse(1L, "newUser", "New Name", "new@email.com", Role.USER);

        Mockito.when(authService.register(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.email").value("new@email.com"))
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    void when_login_then_return_auth_response() throws Exception {
        LoginRequest request = new LoginRequest("TestUser", "password123");
        AuthResponse response = new AuthResponse("message", "Bearer ", "newAccessToken", "user", "newRefreshToken");

        Mockito.when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("newAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("newRefreshToken"));
    }

    @Test
    void when_refresh_then_return_new_tokens() throws Exception {
        RefreshRequest request = new RefreshRequest("oldRefreshToken");
        AuthResponse response = new AuthResponse("message", "Bearer ", "newAccessToken", "userName", "newRefreshToken");

        Mockito.when(authService.refresh(any(RefreshRequest.class), any(CustomUserDetails.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("newAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("newRefreshToken"));
    }

    @Test
    void when_logout_then_return_message() throws Exception {
        ApiMessageDto response = new ApiMessageDto("Logged out successfully");

        Mockito.when(authService.logout(any(HttpServletRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/logout")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    void when_register_with_invalid_data_then_return_bad_request() throws Exception {
        String invalidJson = """
            {
                "username": "",
                "email": "invalid-email",
                "password": "123"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void when_refresh_without_authentication_then_return_unauthorized() throws Exception {
        RefreshRequest request = new RefreshRequest("someRefreshToken");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}