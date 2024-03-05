package mate.academy.carsharing.dto.rental;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateRentalRequestDto(
        @NotNull
        LocalDate rentalDate,
        @NotNull
        LocalDate returnDate,
        @NotNull
        Long carId,
        @NotNull
        Long userId) {
}
