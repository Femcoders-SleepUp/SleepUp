package com.SleepUp.SU.user.user;

import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.role.Role;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.SleepUp.SU.user.CustomUserDetails;
import com.SleepUp.SU.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private UserRepository userRepository;

    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        User savedUser = userRepository.findByUsername("TestUser").get();
        customUserDetails = new CustomUserDetails(savedUser);
    }


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
