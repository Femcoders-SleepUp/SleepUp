package com.SleepUp.SU.user.user;

import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.utils.dto.ApiMessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "Operations related to the authenticated user's account")
@RestController
@RequestMapping("/users/me")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class UserUserController {

    private final UserUserService userUserService;

    @GetMapping
    @Operation(summary = "Get Logged-in User Info", description = "Retrieve information about the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved user information"),
                    @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getLoggedUser(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        return userUserService.getLoggedUser(customUserDetails.getId());
    }

    @PutMapping
    @Operation(summary = "Update Logged-in User Info", description = "Update the information of the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated user information"),
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/UserFound"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.OK)
    public UserResponse putLoggedUser(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                      @RequestBody @Valid UserRequest userRequest){
        return userUserService.updateLoggedUser(userRequest, customUserDetails.getId());
    }

    @DeleteMapping
    @Operation(summary = "Delete Logged-in User Account", description = "Delete the account of the currently authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully deleted user account"),
                    @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/UserFound"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.OK)
    public ApiMessageDto deleteLoggedUser(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        userUserService.deleteMyUser(customUserDetails.getId());
        return new ApiMessageDto("Account deleted!!");
    }
}