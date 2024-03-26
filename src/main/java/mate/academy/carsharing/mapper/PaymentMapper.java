package mate.academy.carsharing.mapper;

import mate.academy.carsharing.config.MapperConfig;
import mate.academy.carsharing.dto.payment.CreatePaymentRequestDto;
import mate.academy.carsharing.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {

    @Mapping(target = "rentalId", source = "rental.id")
    @Mapping(target = "userId", source = "rental.user.id")
    PaymentResponseDto toDto(Payment payment);

    Payment toModel(CreatePaymentRequestDto requestDto);
}
