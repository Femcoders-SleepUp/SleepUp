package com.SleepUp.SU.user.user;

import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
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
import com.SleepUp.SU.user.entity.CustomUserDetails;
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
    private UserUserServiceImpl userUserServiceImpl;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private CustomUserDetails customUserDetails;

    private static final String userPath = "/api/users/me";

    @BeforeEach
    void setUp() {
        User savedUser = userRepository.findByUsername("TestUser").get();
        customUserDetails = new CustomUserDetails(savedUser);
    }

    @Nested
    class getLoggedUser{

        @Test
        void getLoggedUser_whenUserIsAuthenticated_shouldReturnCurrentUserInfo() throws Exception {

            UserResponse response = new UserResponse(99L, "testUser", "Test Name", "test@email.com", Role.USER);

            when(userUserServiceImpl.getLoggedUser(anyLong())).thenReturn(response);

            mockMvc.perform(get(userPath)
                            .with(user(customUserDetails))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("testUser"))
                    .andExpect(jsonPath("$.name").value("Test Name"))
                    .andExpect(jsonPath("$.email").value("test@email.com"));
        }

        @Test
        void getLoggedUser_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get(userPath)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class updateLoggedUser{

        @Test
        void
        updateLoggedUser_whenAuthenticated_shouldUpdateAndReturnUpdatedUser()throws Exception {
            UserRequest request = new UserRequest("updatedUser", "updateName", "updated@email.com", "newPassword");
            UserResponse response = new UserResponse(99L, "updatedUser", "updateName", "updated@email.com", Role.USER);

            when(userUserServiceImpl.updateLoggedUser(any(UserRequest.class), anyLong())).thenReturn(response);

            mockMvc.perform(
                            put(userPath)
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
                            put(userPath)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonRequest)
                                    .accept(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void updateLoggedUser_whenServiceThrowsException_shouldReturnBadRequest() throws Exception {
            UserRequest request = new UserRequest("updatedUser", "updateName",  "updated@email.com", "newPassword");

            when(userUserServiceImpl.updateLoggedUser(request, 99L))
                    .thenThrow(new IllegalArgumentException("Invalid update"));

            String jsonRequest = """
        {
            "username": "updatedUser",
            "email": "updated@email.com",
            "password": "newPassword"
        }
        """;

            mockMvc.perform(
                            put(userPath)
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
        void deleteLoggedUser_whenAuthenticated_shouldDeleteAccountAndReturnSuccess() throws Exception {
            mockMvc.perform(delete(userPath)
                            .with(user(customUserDetails))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Account deleted!!"));
        }

        @Test
        void deleteLoggedUser_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(delete(userPath)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
