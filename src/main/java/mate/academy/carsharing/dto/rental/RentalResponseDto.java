package mate.academy.carsharing.dto.rental;

import java.time.LocalDate;

public record RentalResponseDto(
        Long id,
        LocalDate rentalDate,
        LocalDate returnDate,
        LocalDate actualReturnDate,
        Long carId,
        Long userId) {
}
