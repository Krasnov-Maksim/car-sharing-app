package mate.academy.carsharing.dto.user;

import java.util.Set;
import mate.academy.carsharing.model.Role;

public record UserResponseDtoWithRoles(
        Long id,
        String email,
        String firstName,
        String lastName,
        Set<Role> roles) {
}

