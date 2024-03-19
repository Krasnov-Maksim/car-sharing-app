package mate.academy.carsharing.service.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.dto.payment.CreatePaymentRequestDto;
import mate.academy.carsharing.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.dto.payment.PaymentSearchParametersDto;
import mate.academy.carsharing.exception.PaymentException;
import mate.academy.carsharing.mapper.PaymentMapper;
import mate.academy.carsharing.model.Payment;
import mate.academy.carsharing.model.Rental;
import mate.academy.carsharing.model.Role;
import mate.academy.carsharing.model.User;
import mate.academy.carsharing.repository.payment.PaymentRepository;
import mate.academy.carsharing.repository.payment.PaymentSpecificationBuilder;
import mate.academy.carsharing.repository.rental.RentalRepository;
import mate.academy.carsharing.repository.role.RoleRepository;
import mate.academy.carsharing.repository.user.UserRepository;
import mate.academy.carsharing.service.NotificationService;
import mate.academy.carsharing.service.PaymentService;
import mate.academy.carsharing.stripe.StripeSessionProvider;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StripePaymentServiceImpl implements PaymentService {
    private static final BigDecimal CONVERT_TO_CENT = BigDecimal.valueOf(100L);
    private static final BigDecimal FINE_MULTIPLIER = BigDecimal.valueOf(1.50);

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NotificationService notificationService;
    private final PaymentSpecificationBuilder paymentSpecificationBuilder;
    private final StripeSessionProvider stripeSessionProvider;

    @Override
    public PaymentResponseDto save(CreatePaymentRequestDto requestDto) {
        Rental rental = getRentalById(requestDto.rentalId());
        User user = rental.getUser();
        BigDecimal totalSum = calculateTotalSum(rental);
        Payment payment = createPayment(rental, totalSum);
        try {
            Session session =
                    stripeSessionProvider.createStripeSession(totalSum, "Rental Payment");
            payment.setSessionId(session.getId());
            payment.setSessionUrl(new URL(session.getUrl()));
            notificationService.sendNotification(user.getId(),
                    "Payment URL: " + payment.getSessionUrl());
        } catch (StripeException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    public List<PaymentResponseDto> search(String email,
            PaymentSearchParametersDto searchParameters, Pageable pageable) {
        User user = getUserByEmail(email);
        Role roleManager = getRoleByName(Role.RoleName.ROLE_MANAGER);
        PaymentSearchParametersDto checkedSearchParameters;
        if (user.getRoles().contains(roleManager)) {
            checkedSearchParameters = searchParameters;
        } else {
            String[] userIdsArray = {user.getId().toString()};
            checkedSearchParameters = new PaymentSearchParametersDto(userIdsArray);
        }
        Specification<Payment> paymentSpecification =
                paymentSpecificationBuilder.build(checkedSearchParameters);
        return paymentRepository.findAll(paymentSpecification, pageable)
                .stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public PaymentResponseDto processSuccessfulPayment(String sessionId) {
        Payment payment = getPaymentBySessionId(sessionId);
        Session session;
        try {
            session = stripeSessionProvider.retrieveSession(payment.getSessionId());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
        if ("paid".equalsIgnoreCase(session.getPaymentStatus())) {
            payment.setStatus(Payment.Status.PAID);
            String message = String.format("Payment with id: %d for the amount: %s successful!",
                    payment.getId(),
                    payment.getAmountToPay().divide(CONVERT_TO_CENT, RoundingMode.HALF_UP));
            notificationService.sendNotification(payment.getRental().getUser().getId(), message);
        }
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponseDto processCanceledPayment(String sessionId) {
        Payment payment = getPaymentBySessionId(sessionId);
        payment.setStatus(Payment.Status.CANCEL);
        notificationService.sendNotification(payment.getRental().getUser().getId(),
                "Payment failure! The payment can be made later within 24 hours!");
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Scheduled(cron = "0 * * * * *")
    public void checkExpiredStripeSessions() {
        List<Payment> payments = paymentRepository.findAllByStatus(Payment.Status.PENDING);
        for (Payment payment : payments) {
            try {
                Session session = stripeSessionProvider.retrieveSession(payment.getSessionId());
                if ("expired".equals(session.getStatus())) {
                    payment.setStatus(Payment.Status.EXPIRED);
                    paymentRepository.save(payment);
                }
            } catch (StripeException e) {
                throw new RuntimeException("Can't retrieve session for payment:" + payment, e);
            }
        }
    }

    @Override
    public PaymentResponseDto renewPaymentSession(Long paymentId, String email) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
                () -> new EntityNotFoundException("Can't find payment with id: " + paymentId));
        Rental rental = payment.getRental();
        User userByAuthentication = getUserByEmail(email);
        User userByRental = rental.getUser();
        Role roleManager = getRoleByName(Role.RoleName.ROLE_MANAGER);
        if (!userByAuthentication.getRoles().contains(roleManager)) {
            if (!userByRental.getEmail().equals(email)) {
                throw new PaymentException("You do not have permission to renew this session");
            }
        }
        if (payment.getStatus().equals(Payment.Status.PAID)) {
            throw new PaymentException("This payment session cannot be renewed");
        }
        if (payment.getStatus().equals(Payment.Status.PENDING)) {
            throw new PaymentException("No need to renew. The session is active");
        }
        try {
            Session newSession = stripeSessionProvider
                    .createStripeSession(calculateTotalSum(rental), "Rental repayment");
            payment.setStatus(Payment.Status.PENDING);
            payment.setSessionId(newSession.getId());
            payment.setSessionUrl(new URL(newSession.getUrl()));
            paymentRepository.save(payment);
            return paymentMapper.toDto(payment);
        } catch (StripeException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private Payment getPaymentBySessionId(String sessionId) {
        return paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Can't find payment with session id: "
                        + sessionId));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Can't find user with email: " + email));
    }

    private Role getRoleByName(Role.RoleName roleName) {
        return roleRepository.findByName(roleName).orElseThrow(
                () -> new EntityNotFoundException("Can't find role with name: " + roleName.name())
        );
    }

    private Rental getRentalById(Long rentalId) {
        return rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id: " + rentalId));
    }

    private Payment createPayment(Rental rental, BigDecimal totalSum) {
        Payment payment = new Payment();
        payment.setAmountToPay(totalSum);
        payment.setStatus(Payment.Status.PENDING);
        payment.setRental(rental);
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

    private BigDecimal calculateTotalSum(Rental rental) {
        BigDecimal alreadyPaid =
                paymentRepository.getSumByRentalAndPaymentStatus(rental, Payment.Status.PAID);
        if (Objects.isNull(alreadyPaid)) {
            alreadyPaid = BigDecimal.ZERO;
        }
        BigDecimal baseSum = rental.getCar().getDailyFee().multiply(CONVERT_TO_CENT)
                .multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(
                        rental.getRentalDate(), rental.getReturnDate())));
        BigDecimal overdueSum = rental.getCar().getDailyFee().multiply(CONVERT_TO_CENT)
                .multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(
                        rental.getReturnDate(), LocalDate.now())))
                .multiply(FINE_MULTIPLIER);
        BigDecimal totalSum = baseSum.add(overdueSum).subtract(alreadyPaid);
        totalSum = totalSum.setScale(0, RoundingMode.CEILING);
        return totalSum.doubleValue() < 0 ? BigDecimal.ZERO : totalSum;
    }
}
