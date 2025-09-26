package com.SleepUp.SU.user;

import com.SleepUp.SU.user.admin.UserAdminController;
import com.SleepUp.SU.user.admin.UserAdminServiceImpl;
import com.SleepUp.SU.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @InjectMocks
    private UserAdminController userAdminController;

    @Mock
    private UserAdminServiceImpl userAdminServiceImpl;

    @Test
    void should_returnAllUsers() {
        List<UserResponse> users = List.of(
                new UserResponse(1L,"", "userOne", "user1@test.com", null),
                new UserResponse(2L, "","userTwo", "user2@test.com", null)
        );

        when(userAdminServiceImpl.getAllUsers()).thenReturn(users);

        List<UserResponse> response = userAdminController.listAllUsers();


        assertEquals(2, response.size());
    }
}
