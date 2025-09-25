package com.SleepUp.SU.user.admin;

import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserAdminService userAdminService;

    @GetMapping
    public List<UserResponse> listAllUsers() {
        return userAdminService.getAllUsers();
    }

    @GetMapping("/{id}")
        public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse userResponse = userAdminService.getUserById(id);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping
        public ResponseEntity<UserResponse> createUser(@RequestBody UserRequestAdmin userRequestAdmin) {
        UserResponse newUser = userAdminService.createUser(userRequestAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PutMapping("/{id}")
        public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserRequestAdmin userRequestAdmin) {
        UserResponse updatedUser = userAdminService.updateUser(id, userRequestAdmin);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userAdminService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}

