package mate.academy.carsharing.dto.payment;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequestDto(
        @NotNull
        Long rentalId) {
}
