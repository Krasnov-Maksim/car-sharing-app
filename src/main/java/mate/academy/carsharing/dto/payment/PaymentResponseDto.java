package mate.academy.carsharing.dto.payment;

import java.math.BigDecimal;

public record PaymentResponseDto(
        Long id,
        Long rentalId,
        Long userId,
        String status,
        String type,
        String sessionUrl,
        String sessionId,
        BigDecimal amountToPay) {
}
