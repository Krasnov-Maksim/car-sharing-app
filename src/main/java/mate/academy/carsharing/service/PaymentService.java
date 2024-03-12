package mate.academy.carsharing.service;

import mate.academy.carsharing.dto.payment.CreatePaymentRequestDto;
import mate.academy.carsharing.dto.payment.PaymentResponseDto;

public interface PaymentService {
    PaymentResponseDto save(String email, CreatePaymentRequestDto requestDto);
}