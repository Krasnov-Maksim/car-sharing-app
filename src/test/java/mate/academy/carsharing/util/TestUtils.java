package mate.academy.carsharing.util;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Set;
import mate.academy.carsharing.dto.car.CarResponseDto;
import mate.academy.carsharing.dto.car.CreateCarRequestDto;
import mate.academy.carsharing.dto.payment.PaymentResponseDto;
import mate.academy.carsharing.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.dto.rental.RentalResponseDto;
import mate.academy.carsharing.dto.user.UserRegistrationRequestDto;
import mate.academy.carsharing.dto.user.UserResponseDto;
import mate.academy.carsharing.dto.user.UserResponseDtoWithRoles;
import mate.academy.carsharing.dto.user.UserUpdateInfoRequestDto;
import mate.academy.carsharing.dto.user.UserUpdateRoleRequestDto;
import mate.academy.carsharing.model.Car;
import mate.academy.carsharing.model.Payment;
import mate.academy.carsharing.model.Rental;
import mate.academy.carsharing.model.Role;
import mate.academy.carsharing.model.User;
import mate.academy.carsharing.telegram.TelegramUserState;

public final class TestUtils {
    public static final Long VALID_ID = 1L;
    public static final Long NOT_VALID_ID = -1L;
    public static final String VALID_MODEL = "Valid Model";
    public static final String VALID_BRAND = "Valid Brand";
    public static final Car.Type VALID_TYPE = Car.Type.SUV;
    public static final Integer VALID_INVENTORY = 10;
    public static final BigDecimal VALID_DAILY_FEE = BigDecimal.TEN;
    public static final String VALID_EMAIL = "test@email.com";
    public static final String NOT_VALID_EMAIL = "NOT VALID EMAIL";
    public static final String VALID_FIRST_NAME = "First Name";
    public static final String VALID_LAST_NAME = "Last Name";
    public static final String VALID_PASSWORD = "Password";
    public static final LocalDate VALID_RENTAL_DATE = LocalDate.now();
    public static final LocalDate VALID_RETURN_DATE = LocalDate.now().plusDays(5);
    public static final LocalDate VALID_ACTUAL_RETURN_DATE = LocalDate.now().plusDays(4);
    public static final String VALID_SESSION_ID = "stripe_session_id";
    public static final String VALID_SESSION_URL = "http://payment.url";
    public static final Long VALID_CHAT_ID = 123456L;
    public static final String REGISTRATION_SUCCESS = """
            Registration success. I know next commands: '/checkRentals'
            """;
    public static final String VALID_NEW_LAST_NAME = "New Last Name";
    public static final Role.RoleName VALID_ROLE = Role.RoleName.ROLE_CUSTOMER;

    private TestUtils() {
    }

    public static Car createValidCar() {
        Car car = new Car();
        car.setId(VALID_ID);
        car.setModel(VALID_MODEL);
        car.setBrand(VALID_BRAND);
        car.setType(VALID_TYPE);
        car.setInventory(VALID_INVENTORY);
        car.setDailyFee(VALID_DAILY_FEE);
        return car;
    }

    public static CreateCarRequestDto createValidCarRequestDto() {
        return new CreateCarRequestDto(
                VALID_MODEL,
                VALID_BRAND,
                VALID_TYPE,
                VALID_INVENTORY,
                VALID_DAILY_FEE
        );
    }

    public static CarResponseDto createValidCarResponseDto() {
        CarResponseDto carResponseDto = new CarResponseDto();
        carResponseDto.setId(VALID_ID);
        carResponseDto.setModel(VALID_MODEL);
        carResponseDto.setBrand(VALID_BRAND);
        carResponseDto.setType(VALID_TYPE);
        carResponseDto.setInventory(VALID_INVENTORY);
        carResponseDto.setDailyFee(VALID_DAILY_FEE);
        return carResponseDto;
    }

    public static User createValidUser() {
        User user = new User();
        user.setId(VALID_ID);
        user.setEmail(VALID_EMAIL);
        user.setFirstName(VALID_FIRST_NAME);
        user.setLastName(VALID_LAST_NAME);
        user.setPassword(VALID_PASSWORD);
        return user;
    }

    public static Rental createValidRental() {
        Rental rental = new Rental();
        rental.setId(VALID_ID);
        rental.setRentalDate(VALID_RENTAL_DATE);
        rental.setReturnDate(VALID_RETURN_DATE);
        rental.setCar(createValidCar());
        rental.setUser(createValidUser());
        return rental;
    }

    public static CreateRentalRequestDto createValidRentalRequestDto() {
        return new CreateRentalRequestDto(
                VALID_RENTAL_DATE,
                VALID_RETURN_DATE,
                VALID_ID,
                VALID_ID
        );
    }

    public static RentalResponseDto createValidRentalResponseDto() {
        return new RentalResponseDto(
                VALID_ID,
                VALID_RENTAL_DATE,
                VALID_RETURN_DATE,
                null,
                VALID_ID,
                VALID_ID
        );
    }

    public static Payment createExpiredPayments() {
        Payment payment = new Payment();
        payment.setId(VALID_ID);
        payment.setRental(createValidRental());
        try {
            payment.setSessionUrl(new URL("http://localhost:8080/"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        payment.setStatus(Payment.Status.EXPIRED);
        payment.setSessionId("SESSION ID");
        payment.setType(Payment.Type.PAYMENT);
        payment.setAmountToPay(BigDecimal.TEN);
        return payment;
    }

    public static RentalResponseDto returnedRentalResponseDto() {
        return new RentalResponseDto(
                VALID_ID,
                VALID_RENTAL_DATE,
                VALID_RETURN_DATE,
                VALID_ACTUAL_RETURN_DATE,
                VALID_ID,
                VALID_ID
        );
    }

    public static Rental createOverdueRental() {
        Rental rental = new Rental();
        rental.setId(VALID_ID);
        rental.setRentalDate(LocalDate.now().minusDays(5));
        rental.setReturnDate(VALID_RETURN_DATE);
        rental.setCar(createValidCar());
        rental.setUser(createValidUser());
        rental.setDeleted(false);
        return rental;
    }

    public static Payment createValidPayment() throws MalformedURLException {
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

    public static PaymentResponseDto createValidPaymentResponseDto() {
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

    public static TelegramUserState createValidTelegramUserState() {
        return new TelegramUserState(
                VALID_ID,
                VALID_EMAIL,
                true
        );
    }

    public static UserRegistrationRequestDto createValidUserRegistrationRequestDto() {
        return new UserRegistrationRequestDto(
                VALID_EMAIL,
                VALID_PASSWORD,
                VALID_PASSWORD,
                VALID_FIRST_NAME,
                VALID_LAST_NAME
        );
    }

    public static UserResponseDto createValidUserResponseDto() {
        return new UserResponseDto(
                VALID_ID,
                VALID_EMAIL,
                VALID_FIRST_NAME,
                VALID_LAST_NAME
        );
    }

    public static UserUpdateRoleRequestDto createValidUserUpdateRoleRequestDto() {
        return new UserUpdateRoleRequestDto(createValidRole(VALID_ID, Role.RoleName.ROLE_CUSTOMER));
    }

    public static Role createValidRole(Long roleId, Role.RoleName roleName) {
        Role role = new Role();
        role.setId(roleId);
        role.setName(roleName);
        return role;
    }

    public static UserResponseDtoWithRoles createValidUserWithRoleResponseDto() {
        return new UserResponseDtoWithRoles(
                VALID_ID,
                VALID_EMAIL,
                VALID_FIRST_NAME,
                VALID_LAST_NAME,
                Set.of(createValidRole(VALID_ID, Role.RoleName.ROLE_CUSTOMER))
        );
    }

    public static UserUpdateInfoRequestDto createValidUserUpdateInfoRequestDto() {
        return new UserUpdateInfoRequestDto(
                VALID_PASSWORD,
                VALID_FIRST_NAME,
                VALID_NEW_LAST_NAME
        );
    }
}
