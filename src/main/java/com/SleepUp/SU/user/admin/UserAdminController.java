package com.SleepUp.SU.user.admin;

import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Admin", description = "Operations related to user management by admin")
@RestController
@RequestMapping("/api/users/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserAdminService userAdminService;

    @GetMapping
    @Operation(summary = "List All Users", description = "Retrieve a list of all users in the system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
    })
    public List<UserResponse> listAllUsers() {
        return userAdminService.getAllUsers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get User by ID", description = "Retrieve detailed information about a specific user by their ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved user details"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/UserFound"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
        public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse userResponse = userAdminService.getUserById(id);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping
    @Operation(summary = "Create New User", description = "Create a new user with the provided details.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created a new user"),
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
        public ResponseEntity<UserResponse> createUser(@RequestBody UserRequestAdmin userRequestAdmin) {
        UserResponse newUser = userAdminService.createUser(userRequestAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update User", description = "Update the details of an existing user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated user details"),
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/UserFound"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
        public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserRequestAdmin userRequestAdmin) {
        UserResponse updatedUser = userAdminService.updateUser(id, userRequestAdmin);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete User", description = "Delete a user by their ID.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successfully deleted the user"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/UserFound"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userAdminService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}