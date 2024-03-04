package mate.academy.carsharing.dto.user;

import jakarta.validation.constraints.NotNull;

public record UserUpdateInfoRequestDto(
        @NotNull(message = "First name cannot be null")
        String firstName,
        @NotNull(message = "Last name cannot be null")
        String lastName) {
}
