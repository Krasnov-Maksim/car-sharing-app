package mate.academy.carsharing.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserLoginRequestDto(
        @NotNull
        @Email
        String email,
        @NotNull
        @Size(min = 8, message = "Password must contain at least 8 characters")
        String password) {
}
