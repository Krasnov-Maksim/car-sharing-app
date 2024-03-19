package mate.academy.carsharing.dto.user;

import jakarta.validation.constraints.Size;

public record UserUpdateInfoRequestDto(
        String firstName,
        String lastName,
        @Size(min = 8, message = PASSWORD_MUST_CONTAIN_AT_LEAST_8_CHARACTERS)
        String password) {
    private static final String PASSWORD_MUST_CONTAIN_AT_LEAST_8_CHARACTERS
            = "Password must contain at least 8 characters";
}
