package com.SleepUp.SU.user.user;

import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserUserControllerTest {

    public static final String BASE = "/users";
    public static final String LOGGED_USER = BASE + "/me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUserController userUserController;

    @Autowired
    private UserUserServiceImpl userUserServiceImpl;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        User savedUser = userRepository.findByUsername("TestUser")
                .orElseThrow(() -> new RuntimeException("TestUser not found"));
        customUserDetails = new CustomUserDetails(savedUser);
    }

    @Nested
    class getLoggedUser {

        @Test
        void getLoggedUser_whenUserIsAuthenticated_shouldReturnCurrentUserInfo() throws Exception {
            mockMvc.perform(get(LOGGED_USER)
                            .with(user(customUserDetails))
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("TestUser"))
                    .andExpect(jsonPath("$.name").value("nameTest"))
                    .andExpect(jsonPath("$.email").value("usertnest@test.com"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        void getLoggedUser_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get(LOGGED_USER)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class updateLoggedUser {

        @Test
        void updateLoggedUser_whenAuthenticated_shouldUpdateAndReturnUpdatedUser() throws Exception {
            UserRequest request = new UserRequest("updatedUser", "updateName", "updated@email.com", "newPassword");

            mockMvc.perform(
                            put(LOGGED_USER)
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
        void updateLoggedUser_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
            String jsonRequest = """
            {
                "username": "updatedUser",
                "email": "updated@email.com",
                "password": "newPassword"
            }
            """;

            mockMvc.perform(
                            put(LOGGED_USER)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonRequest)
                                    .accept(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void updateLoggedUser_whenServiceThrowsException_shouldReturnBadRequest() throws Exception {
            String jsonRequest = """
            {
                "username": "updatedUser",
                "email": "updated@email.com",
                "password": "newPassword"
            }
            """;

            mockMvc.perform(
                            put(LOGGED_USER)
                                    .with(user(customUserDetails))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonRequest)
                                    .accept(MediaType.APPLICATION_JSON)
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class deleteLoggedUser {

        @Test
        void deleteLoggedUser_whenAuthenticated_shouldDeleteAccountAndReturnSuccess() throws Exception {
            mockMvc.perform(delete(LOGGED_USER)
                            .with(user(customUserDetails))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Account deleted!!"));
        }

        @Test
        void deleteLoggedUser_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(delete(LOGGED_USER)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}