package mate.academy.carsharing.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mate.academy.carsharing.validation.FieldMatch;

@FieldMatch(
        firstField = "password",
        secondField = "repeatPassword",
        message = "Fields 'password' and 'repeatPassword' must match."
)
public record UserRegistrationRequestDto(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = PASSWORD_MUST_CONTAIN_AT_LEAST_8_CHARACTERS)
        @Size(min = 8, message = PASSWORD_MUST_CONTAIN_AT_LEAST_8_CHARACTERS)
        String password,
        @NotBlank(message = PASSWORD_MUST_CONTAIN_AT_LEAST_8_CHARACTERS)
        @Size(min = 8, message = PASSWORD_MUST_CONTAIN_AT_LEAST_8_CHARACTERS)
        String repeatPassword,
        @NotNull(message = "First name cannot be null")
        String firstName,
        @NotNull(message = "Last name cannot be null")
        String lastName) {
    private static final String PASSWORD_MUST_CONTAIN_AT_LEAST_8_CHARACTERS
            = "Password must contain at least 8 characters";
}
