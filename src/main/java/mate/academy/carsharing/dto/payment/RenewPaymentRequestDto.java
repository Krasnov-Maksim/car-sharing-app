package mate.academy.carsharing.dto.payment;

import jakarta.validation.constraints.NotNull;

public record RenewPaymentRequestDto(
        @NotNull
        Long paymentId) {
}
