package com.SleepUp.SU.user.admin;

import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userService;

    @GetMapping
    public List<UserResponse> listAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse userResponse = userService.getUserById(id);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping
    @PreAuthorize("hazRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequestAdmin userRequestAdmin) {
        UserResponse newUser = userService.createUser(userRequestAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

}

