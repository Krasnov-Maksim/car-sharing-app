package mate.academy.carsharing.service;

import mate.academy.carsharing.dto.user.UserRegistrationRequestDto;
import mate.academy.carsharing.dto.user.UserResponseDto;
import mate.academy.carsharing.dto.user.UserResponseDtoWithRoles;
import mate.academy.carsharing.dto.user.UserUpdateRoleRequestDto;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto);

    UserResponseDtoWithRoles updateUserRole(String userEmail,
            UserUpdateRoleRequestDto userUpdateRoleRequestDto);

}
