package mate.academy.carsharing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.annotation.UserRoleDescription;
import mate.academy.carsharing.dto.payment.CreatePaymentRequestDto;
import mate.academy.carsharing.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.dto.payment.PaymentSearchParametersDto;
import mate.academy.carsharing.dto.payment.RenewPaymentRequestDto;
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
    @UserRoleDescription
    @Operation(summary = "Create new payment.", description = "Create new payment session.")
    @PostMapping()
    public PaymentResponseDto createPayment(
            @RequestBody @Valid CreatePaymentRequestDto requestDto) {
        return paymentService.save(requestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_CUSTOMER')")
    @UserRoleDescription
    @Operation(summary = "Search payments.", description = "User can view own payments. "
            + "Admin can view all payments.")
    @Parameter(name = "page", description = "page index, default value = 0")
    @Parameter(name = "size", description = "elements per page, default value = 20")
    @Parameter(name = "sort", description = "sort criteria", example = "amountToPay,Desc")
    @GetMapping("/search")
    public List<PaymentResponseDto> searchPayments(Authentication authentication,
            PaymentSearchParametersDto searchParameters, Pageable pageable) {
        return paymentService.search(authentication.getName(), searchParameters, pageable);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("permitAll()")
    @UserRoleDescription
    @Operation(summary = "Stripe redirection endpoint marks payment as success.",
            description = "Mark payment as success, send message to Telegram.")
    @GetMapping("/success")
    public PaymentResponseDto processSuccessfulPayment(
            @RequestParam(name = "session_id") String sessionId) {
        return paymentService.processSuccessfulPayment(sessionId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("permitAll()")
    @UserRoleDescription
    @Operation(summary = "Stripe redirection endpoint marks payment as canceled",
            description = "Marks payment as canceled, send message to Telegram.")
    @GetMapping("/cancel")
    public PaymentResponseDto processCanceledPayment(
            @RequestParam(name = "session_id") String sessionId) {
        return paymentService.processCanceledPayment(sessionId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_CUSTOMER')")
    @UserRoleDescription
    @Operation(summary = "Renew expired session.",
            description = "User can renew expired session and get new payment link.")
    @PostMapping("/renew")
    public PaymentResponseDto renewPaymentSession(
            @RequestBody @Valid RenewPaymentRequestDto requestDto,
            Authentication authentication) {
        return paymentService.renewPaymentSession(requestDto.paymentId(), authentication.getName());
    }
}
