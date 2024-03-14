package mate.academy.carsharing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.dto.payment.CreatePaymentRequestDto;
import mate.academy.carsharing.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.dto.payment.PaymentSearchParametersDto;
import mate.academy.carsharing.service.PaymentService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment managing", description = "Endpoints for managing payments")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Create new payment", description = "Create new payment session")
    @PostMapping()
    public PaymentResponseDto createPayment(
            @RequestBody @Valid CreatePaymentRequestDto requestDto) {
        return paymentService.save(requestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_CUSTOMER')")
    @GetMapping("/search")
    public List<PaymentResponseDto> searchPayments(Authentication authentication,
            PaymentSearchParametersDto searchParameters, Pageable pageable) {
        return paymentService.search(authentication.getName(), searchParameters, pageable);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("permitAll()")
    @Operation(summary = "Stripe redirection endpoint",
            description = "Mark payment as success, send message to Telegram")
    @GetMapping("/success")
    public PaymentResponseDto processSuccessfulPayment(
            @RequestParam(name = "session_id") String sessionId) {
        return paymentService.processSuccessfulPayment(sessionId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("permitAll()")
    @Operation(summary = "Stripe redirection endpoint",
            description = "Mark payment as canceled, send message to Telegram")
    @GetMapping("/cancel")
    public PaymentResponseDto processCanceledPayment(
            @RequestParam(name = "session_id") String sessionId) {
        return paymentService.processCanceledPayment(sessionId);
    }
}
