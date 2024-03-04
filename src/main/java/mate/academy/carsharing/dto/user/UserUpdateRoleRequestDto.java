package mate.academy.carsharing.dto.user;

import jakarta.validation.constraints.NotNull;
import mate.academy.carsharing.model.Role;

public record UserUpdateRoleRequestDto(@NotNull Role role) {
}
