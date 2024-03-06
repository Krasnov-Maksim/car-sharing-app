package mate.academy.carsharing.dto.rental;

public record RentalSearchParametersDto(
        String[] user_id,
        String[] is_active) {
}
