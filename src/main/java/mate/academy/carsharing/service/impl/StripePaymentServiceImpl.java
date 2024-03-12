package mate.academy.carsharing.service.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.dto.payment.CreatePaymentRequestDto;
import mate.academy.carsharing.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.mapper.PaymentMapper;
import mate.academy.carsharing.model.Payment;
import mate.academy.carsharing.model.Rental;
import mate.academy.carsharing.model.User;
import mate.academy.carsharing.repository.payment.PaymentRepository;
import mate.academy.carsharing.repository.rental.RentalRepository;
import mate.academy.carsharing.repository.user.UserRepository;
import mate.academy.carsharing.service.NotificationService;
import mate.academy.carsharing.service.PaymentService;
import mate.academy.carsharing.stripe.StripeSessionProvider;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StripePaymentServiceImpl implements PaymentService {
    private static final BigDecimal CONVERT_TO_CENT = BigDecimal.valueOf(100L);

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final StripeSessionProvider stripeSessionProvider;

    @Override
    public PaymentResponseDto save(String email, CreatePaymentRequestDto requestDto) {
        User user = getUserByEmail(email);
        Long userId = user.getId();
        Rental rental = getRentalByIdAndUserId(requestDto.rentalId(), userId);
        Payment payment = createPayment(rental);
        try {
            Session session = stripeSessionProvider.createStripeSession(
                    getTotalSum(rental), "Rental Payment");
            payment.setSessionId(session.getId());
            payment.setSessionUrl(new URL(session.getUrl()));
            notificationService.sendNotification(userId, "Payment URL: " + payment.getSessionUrl());
        } catch (StripeException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Can't find user with email: " + email)
        );
    }

    private Rental getRentalByIdAndUserId(Long rentalId, Long userId) {
        return rentalRepository.findByIdAndUserId(rentalId, userId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can't find rental with id: " + rentalId +
                                " for user with id: " + userId)
                );
    }

    private Payment createPayment(Rental rental) {
        Payment payment = new Payment();
        payment.setAmountToPay(getTotalSum(rental));
        payment.setStatus(Payment.Status.PENDING);
        payment.setRental(rental);
        payment.setUser(rental.getUser());
        if (rental.getReturnDate().isAfter(LocalDate.now())) {
            payment.setType(Payment.Type.PAYMENT);
            return payment;
        }
        if (rental.getActualReturnDate() == null) {
            payment.setType(Payment.Type.FINE);
            return payment;
        }
        payment.setType(rental.getActualReturnDate().isAfter(rental.getReturnDate())
                ? Payment.Type.FINE : Payment.Type.PAYMENT);
        return payment;
    }

    private BigDecimal getTotalSum(Rental rental) {
        return rental.getCar().getDailyFee().multiply(CONVERT_TO_CENT).multiply(
                BigDecimal.valueOf(ChronoUnit.DAYS.between(
                        rental.getRentalDate(), rental.getReturnDate()
                )));
    }

}