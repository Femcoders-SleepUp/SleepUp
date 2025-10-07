package com.SleepUp.SU.auth;

import com.SleepUp.SU.auth.dto.*;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.utils.email.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.hamcrest.Matchers.hasLength;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {

    private static final String REGISTER_PATH = "/auth/register";
    private static final String LOGIN_PATH = "/auth/login";
    private static final String REFRESH_PATH = "/auth/refresh";
    private static final String LOGOUT_PATH = "/auth/logout";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthController authController;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @MockitoBean
    private JavaMailSender mailSender;

    @MockitoBean
    private SpringTemplateEngine templateEngine;

    @MockitoBean
    private EmailService emailService;

    private CustomUserDetails principal;

    @BeforeEach
    void setUp() {
        User savedUser = userRepository.findByUsername("User2")
                .orElseThrow(() -> new RuntimeException("User2 not found"));
        principal = new CustomUserDetails(savedUser);
    }

    @Nested
    class RegisterTests {

        @Test
        void register_validRequest_shouldReturnCreatedUser() throws Exception {
            MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            doNothing().when(emailService).sendWelcomeEmail(any(User.class));
            when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

            UserRequest request = new UserRequest("newUser", "New Name", "new@email.com", "password123");

            mockMvc.perform(post(REGISTER_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("newUser"))
                    .andExpect(jsonPath("$.email").value("new@email.com"))
                    .andExpect(jsonPath("$.name").value("New Name"));

            verify(emailService, times(1)).sendWelcomeEmail(any(User.class));
        }

        @Test
        void register_invalidRequest_shouldReturnBadRequest() throws Exception {
            String invalidJson = """
                {
                    "username": "",
                    "email": "invalid-email",
                    "password": "123"
                }
                """;

            mockMvc.perform(post(REGISTER_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class LoginTests {
        @Test
        void login_validCredentials_shouldReturnAuthResponse() throws Exception {
            LoginRequest request = new LoginRequest(principal.getUsername(), "password123");

            mockMvc.perform(post(LOGIN_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", hasLength(220)))
                    .andExpect(jsonPath("$.refreshToken", hasLength(220)));
        }
    }

    @Nested
    class RefreshTests {
        @Test
        void refresh_withAuthentication_shouldReturnNewTokens() throws Exception {

            LoginRequest loginRequest = new LoginRequest(principal.getUsername(), "password123");

            String responseContent = mockMvc.perform(post(LOGIN_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            String oldRefreshToken = JsonPath.read(responseContent, "$.refreshToken");
            String oldToken = JsonPath.read(responseContent, "$.token");

            RefreshRequest refreshRequest = new RefreshRequest(oldRefreshToken);

            mockMvc.perform(post(REFRESH_PATH)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", hasLength(220)))
                    .andExpect(jsonPath("$.refreshToken", hasLength(220)))
                    .andExpect(jsonPath("$.token", org.hamcrest.Matchers.not(oldToken)))
                    .andExpect(jsonPath("$.refreshToken", org.hamcrest.Matchers.not(oldRefreshToken)));

        }

        @Test
        void refresh_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
            RefreshRequest request = new RefreshRequest("someRefreshToken");

            mockMvc.perform(post(REFRESH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class LogoutTests {

        @Test
        void logout_withAuthentication_shouldReturnMessage() throws Exception {
            LoginRequest loginRequest = new LoginRequest(principal.getUsername(), "password123");

            String loginResponse = mockMvc.perform(post(LOGIN_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn()

                    .getResponse()
                    .getContentAsString();

            String token = JsonPath.read(loginResponse, "$.token");

            mockMvc.perform(post(LOGOUT_PATH)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logout successful"));
        }

    }
}