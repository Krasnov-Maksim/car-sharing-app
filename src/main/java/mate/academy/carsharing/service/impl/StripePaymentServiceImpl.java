package mate.academy.carsharing.service.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.dto.payment.CreatePaymentRequestDto;
import mate.academy.carsharing.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.dto.payment.PaymentSearchParametersDto;
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
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StripePaymentServiceImpl implements PaymentService {
    private static final BigDecimal CONVERT_TO_CENT = BigDecimal.valueOf(100L);

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
        Payment payment = createPayment(rental);
        try {
            Session session = stripeSessionProvider.createStripeSession(
                    getTotalSum(rental), "Rental Payment");
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

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Can't find user with email: " + email)
        );
    }

    private Role getRoleByName(Role.RoleName roleName) {
        return roleRepository.findByName(roleName).orElseThrow(
                () -> new EntityNotFoundException("Can't find role with name: " + roleName.name())
        );
    }

    private Long getUserIdByPayment(Payment payment) {
        Long rentalId = payment.getRental().getId();
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id: " + rentalId)
        );
        return rental.getUser().getId();
    }

    private Rental getRentalById(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can't find rental with id: " + rentalId)
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
