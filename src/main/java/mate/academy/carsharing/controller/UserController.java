package mate.academy.carsharing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.dto.user.UserResponseDto;
import mate.academy.carsharing.dto.user.UserResponseDtoWithRoles;
import mate.academy.carsharing.dto.user.UserUpdateInfoRequestDto;
import mate.academy.carsharing.dto.user.UserUpdateRoleRequestDto;
import mate.academy.carsharing.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users managing", description = "Endpoints to user managing")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update user role", description = "Manager updates role for specify user")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PutMapping("/{id}/role")
    public UserResponseDtoWithRoles updateRole(@PathVariable Long id,
            @RequestBody @Valid UserUpdateRoleRequestDto requestDto) {
        return userService.updateUserRole(id, requestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @Operation(summary = "Update info about user",
            description = "Update firstName, lastName or password")
    @PatchMapping("/me")
    public UserResponseDto updateUserInfo(Authentication authentication,
            @RequestBody @Valid UserUpdateInfoRequestDto requestDto) {
        return userService.updateUserInfo(authentication.getName(), requestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_CUSTOMER')")
    @Operation(summary = "Get user info", description = "Get info about user")
    @GetMapping("/me")
    public UserResponseDtoWithRoles getUserInfo(Authentication authentication) {
        return userService.getUserInfo(authentication.getName());
    }
}
