package com.SleepUp.SU.user;

import com.SleepUp.SU.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Test
    void should_returnAllUsers() {
        List<UserResponse> users = List.of(
                new UserResponse(1L, "userOne", "user1@test.com", null),
                new UserResponse(2L, "userTwo", "user2@test.com", null)
        );

        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserResponse>> response = userController.getAllUsers();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }
}
