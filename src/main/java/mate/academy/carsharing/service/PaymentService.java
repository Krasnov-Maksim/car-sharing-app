package mate.academy.carsharing.service;

import java.util.List;
import mate.academy.carsharing.dto.payment.CreatePaymentRequestDto;
import mate.academy.carsharing.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.dto.payment.PaymentSearchParametersDto;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PaymentResponseDto save(CreatePaymentRequestDto requestDto);

    List<PaymentResponseDto> search(String email, PaymentSearchParametersDto searchParameters,
            Pageable pageable);
}
