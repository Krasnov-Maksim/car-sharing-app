package mate.academy.carsharing.service.impl;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import mate.academy.carsharing.dto.payment.CreatePaymentRequestDto;
import mate.academy.carsharing.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.dto.payment.PaymentSearchParametersDto;
import mate.academy.carsharing.exception.EntityNotFoundException;
import mate.academy.carsharing.mapper.PaymentMapper;
import mate.academy.carsharing.model.Car;
import mate.academy.carsharing.model.Payment;
import mate.academy.carsharing.model.Rental;
import mate.academy.carsharing.model.Role;
import mate.academy.carsharing.model.User;
import mate.academy.carsharing.repository.SpecificationProvider;
import mate.academy.carsharing.repository.payment.PaymentRepository;
import mate.academy.carsharing.repository.payment.PaymentSpecificationBuilder;
import mate.academy.carsharing.repository.payment.PaymentSpecificationProviderManager;
import mate.academy.carsharing.repository.payment.spec.PaymentUserIdSpecificationProvider;
import mate.academy.carsharing.repository.rental.RentalRepository;
import mate.academy.carsharing.repository.role.RoleRepository;
import mate.academy.carsharing.repository.user.UserRepository;
import mate.academy.carsharing.service.NotificationService;
import mate.academy.carsharing.stripe.StripeSessionProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class StripePaymentServiceTest {
    private static final String VALID_EMAIL = "test@email.com";
    private static final String VALID_PASSWORD = "Password";
    private static final String VALID_FIRST_NAME = "First Name";
    private static final String VALID_LAST_NAME = "Last Name";
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = -1L;
    private static final LocalDate VALID_RENTAL_DAY = LocalDate.now();
    private static final LocalDate VALID_RETURN_DAY = LocalDate.now().plusDays(5);
    private static final String VALID_SESSION_ID = "stripe_session_id";
    private static final String VALID_SESSION_URL = "http://payment.url";
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private StripeSessionProvider stripeSessionProvider;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PaymentSpecificationBuilder paymentSpecificationBuilder;
    @InjectMocks
    private StripePaymentServiceImpl stripeService;

    private User createValidUser() {
        User user = new User();
        user.setId(VALID_ID);
        user.setEmail(VALID_EMAIL);
        user.setPassword(VALID_PASSWORD);
        user.setRoles(new HashSet<>());
        user.setLastName(VALID_LAST_NAME);
        user.setFirstName(VALID_FIRST_NAME);
        return user;
    }

    private Rental createValidRental() {
        Rental rental = new Rental();
        rental.setId(VALID_ID);
        rental.setRentalDate(VALID_RENTAL_DAY);
        rental.setReturnDate(VALID_RETURN_DAY);
        Car car = new Car();
        car.setId(VALID_ID);
        car.setDailyFee(BigDecimal.TEN);
        rental.setCar(car);
        rental.setUser(createValidUser());
        rental.setDeleted(false);
        return rental;
    }

    private Payment createValidPayment() throws MalformedURLException {
        Payment payment = new Payment();
        payment.setId(VALID_ID);
        payment.setRental(createValidRental());
        payment.setSessionUrl(new URL(VALID_SESSION_URL));
        payment.setStatus(Payment.Status.PENDING);
        payment.setSessionId(VALID_SESSION_ID);
        payment.setType(Payment.Type.PAYMENT);
        payment.setAmountToPay(BigDecimal.TEN);
        return payment;
    }

    private PaymentResponseDto createValidPaymentResponseDto() {
        return new PaymentResponseDto(
                VALID_ID,
                VALID_ID,
                VALID_ID,
                Payment.Status.PENDING.name(),
                Payment.Type.PAYMENT.name(),
                VALID_SESSION_URL,
                VALID_SESSION_ID,
                BigDecimal.TEN
        );
    }

    @Test
    @DisplayName("save() method works")
    void save_WithValidCreatePaymentRequestDto_ReturnPaymentResponseDto()
            throws MalformedURLException, StripeException {
        Rental rental = createValidRental();
        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto(rental.getId());
        Session mockSession = mock(Session.class);
        when(stripeSessionProvider.createStripeSession(any(BigDecimal.class), any(String.class)))
                .thenReturn(mockSession);
        when(rentalRepository.findById(VALID_ID))
                .thenReturn(Optional.of(rental));
        when(paymentRepository
                .getSumByRentalAndPaymentStatus(any(Rental.class), eq(Payment.Status.PAID)))
                .thenReturn(BigDecimal.ZERO);
        when(mockSession.getId())
                .thenReturn(VALID_SESSION_ID);
        when(mockSession.getUrl())
                .thenReturn(VALID_SESSION_URL);
        doNothing()
                .when(notificationService).sendNotification(anyLong(), anyString());
        Payment payment = createValidPayment();
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);
        when(paymentMapper.toDto(any(Payment.class)))
                .thenReturn(createValidPaymentResponseDto());

        PaymentResponseDto actualResponse = stripeService.save(requestDto);

        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(VALID_SESSION_ID, actualResponse.sessionId());
        Assertions.assertEquals(VALID_SESSION_URL, actualResponse.sessionUrl());
        verify(notificationService).sendNotification(eq(VALID_ID), anyString());
    }

    @Test
    @DisplayName("search() method returns payments for specified users")
    public void search_WithValidPaymentSearchParametersDto_ReturnListOfPaymentResponseDto()
            throws MalformedURLException {
        String[] parameterToSearchForTheseUserIds = {VALID_ID.toString()};
        PaymentSearchParametersDto searchParametersDto = new PaymentSearchParametersDto(
                parameterToSearchForTheseUserIds);
        List<SpecificationProvider<Payment>> listOfSpecificationProviders =
                List.of(new PaymentUserIdSpecificationProvider());
        PaymentSpecificationProviderManager specificationProviderManager =
                new PaymentSpecificationProviderManager(listOfSpecificationProviders);
        PaymentSpecificationBuilder specificationBuilder =
                new PaymentSpecificationBuilder(specificationProviderManager);
        Specification<Payment> specification = specificationBuilder.build(searchParametersDto);

        User validUser = createValidUser();
        Role roleManager = new Role();
        roleManager.setName(Role.RoleName.ROLE_MANAGER);
        Payment validPayment = createValidPayment();
        PaymentResponseDto expected = createValidPaymentResponseDto();
        when(paymentSpecificationBuilder.build(any(PaymentSearchParametersDto.class)))
                .thenReturn(specification);
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(validUser));
        when(roleRepository.findByName(Role.RoleName.ROLE_MANAGER))
                .thenReturn(Optional.of(roleManager));
        when(paymentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<Payment>(List.of(validPayment)));
        when(paymentMapper.toDto(validPayment))
                .thenReturn(expected);

        Pageable pageable = PageRequest.of(0, 20);
        List<PaymentResponseDto> actual = stripeService.search(VALID_EMAIL, searchParametersDto,
                pageable);

        Assertions.assertEquals(List.of(expected), actual);
    }

    @Test
    @DisplayName("processSuccessfulPayment() method work")
    public void processSuccessfulPayment_WithValidSessionId_ReturnPaymentResponseDto()
            throws MalformedURLException {
        Payment payment = createValidPayment();
        payment.setStatus(Payment.Status.PAID);
        when(paymentRepository.findBySessionId(VALID_SESSION_ID))
                .thenReturn(Optional.of(createValidPayment()));
        Session mockSession = mock(Session.class);
        try {
            when(stripeSessionProvider.retrieveSession(VALID_SESSION_ID))
                    .thenReturn(mockSession);
        } catch (StripeException e) {
            fail("retrieveSession error!");
        }
        when(mockSession.getPaymentStatus())
                .thenReturn("paid");
        when(paymentRepository.save(payment))
                .thenReturn(payment);
        PaymentResponseDto expected = new PaymentResponseDto(
                VALID_ID,
                VALID_ID,
                VALID_ID,
                Payment.Status.PAID.name(),
                Payment.Type.PAYMENT.name(),
                VALID_SESSION_URL,
                VALID_SESSION_ID,
                BigDecimal.TEN
        );
        when(paymentMapper.toDto(payment))
                .thenReturn(expected);

        PaymentResponseDto actual = stripeService.processSuccessfulPayment(VALID_SESSION_ID);

        Assertions.assertEquals(expected, actual);
        verify(notificationService).sendNotification(any(Long.class), any(String.class));
    }

    @Test
    @DisplayName("processCanceledPayment() method work")
    public void processCanceledPayment_WithValidSessionId_ReturnPaymentResponseDto()
            throws MalformedURLException {
        Payment payment = createValidPayment();
        payment.setStatus(Payment.Status.CANCEL);
        PaymentResponseDto expected = new PaymentResponseDto(
                VALID_ID,
                VALID_ID,
                VALID_ID,
                Payment.Status.CANCEL.name(),
                Payment.Type.PAYMENT.name(),
                VALID_SESSION_URL,
                VALID_SESSION_ID,
                BigDecimal.TEN
        );
        when(paymentRepository.findBySessionId(VALID_SESSION_ID))
                .thenReturn(Optional.of(createValidPayment()));
        doNothing()
                .when(notificationService).sendNotification(anyLong(), anyString());
        when(paymentRepository.save(payment))
                .thenReturn(payment);
        when(paymentMapper.toDto(payment))
                .thenReturn(expected);

        PaymentResponseDto actual = stripeService.processCanceledPayment(VALID_SESSION_ID);

        Assertions.assertEquals(expected, actual);
        verify(notificationService).sendNotification(any(Long.class), any(String.class));
    }

    @Test
    @DisplayName("renewPaymentSession() method successfully renews a session")
    void renewPaymentSession_WithValidParams_ThenSuccessfulRenewSession()
            throws StripeException, MalformedURLException {
        Payment payment = createValidPayment();
        payment.setStatus(Payment.Status.CANCEL);
        Role roleManager = new Role();
        roleManager.setName(Role.RoleName.ROLE_MANAGER);
        Session mockSession = mock(Session.class);
        when(mockSession.getId())
                .thenReturn("new_stripe_session_id");
        when(mockSession.getUrl())
                .thenReturn("http://new.payment.url");
        PaymentResponseDto expected = new PaymentResponseDto(
                VALID_ID,
                VALID_ID,
                VALID_ID,
                Payment.Status.PENDING.name(),
                Payment.Type.PAYMENT.name(),
                "http://new.payment.url",
                "new_stripe_session_id",
                BigDecimal.TEN
        );
        when(paymentRepository.findById(anyLong()))
                .thenReturn(Optional.of(payment));
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(createValidUser()));
        when(roleRepository.findByName(Role.RoleName.ROLE_MANAGER))
                .thenReturn(Optional.of(roleManager));

        when(stripeSessionProvider.createStripeSession(any(BigDecimal.class), anyString()))
                .thenReturn(mockSession);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentMapper.toDto(any(Payment.class)))
                .thenReturn(expected);

        PaymentResponseDto actualResponse =
                stripeService.renewPaymentSession(VALID_ID, VALID_EMAIL);

        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(expected, actualResponse);
        Assertions.assertEquals("new_stripe_session_id", actualResponse.sessionId());
        Assertions.assertEquals("http://new.payment.url", actualResponse.sessionUrl());
    }

    @Test
    @DisplayName("renewPaymentSession() method with invalid 'paymentId' "
            + "throws EntityNotFoundException")
    void renewPaymentSession_WitInvalidPaymentId_ThrowEntityNotFoundException() {
        when(paymentRepository.findById(INVALID_ID))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class,
                () -> stripeService.renewPaymentSession(INVALID_ID, VALID_EMAIL));
    }

    @Test
    @DisplayName("renewPaymentSession() method with invalid 'email' "
            + "throws EntityNotFoundException")
    void renewPaymentSession_WitInvalidEmail_ThrowEntityNotFoundException()
            throws MalformedURLException {
        Long validPaymentId = VALID_ID;
        String email = "invalid@email.com";
        Payment payment = createValidPayment();

        when(paymentRepository.findById(validPaymentId))
                .thenReturn(Optional.of(payment));
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(RuntimeException.class,
                () -> stripeService.renewPaymentSession(validPaymentId, email));
    }

    @Test
    @DisplayName("checkExpiredStripeSessions() methode updates status for expired sessions")
    void checkExpiredStripeSessions_UpdateExpiredSessionStatus()
            throws StripeException, MalformedURLException {
        List<Payment> pendingPayments = List.of(createValidPayment());
        Session expiredSession = mock(Session.class);
        when(expiredSession.getStatus())
                .thenReturn("expired");
        when(paymentRepository.findAllByStatus(Payment.Status.PENDING))
                .thenReturn(pendingPayments);
        when(stripeSessionProvider.retrieveSession(anyString()))
                .thenReturn(expiredSession);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        stripeService.checkExpiredStripeSessions();

        for (Payment payment : pendingPayments) {
            Assertions.assertEquals(Payment.Status.EXPIRED, payment.getStatus());
        }
    }
}
