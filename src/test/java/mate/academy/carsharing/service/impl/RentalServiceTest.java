package mate.academy.carsharing.service.impl;

import static mate.academy.carsharing.model.Payment.Status;
import static mate.academy.carsharing.util.TestUtils.VALID_ACTUAL_RETURN_DATE;
import static mate.academy.carsharing.util.TestUtils.VALID_EMAIL;
import static mate.academy.carsharing.util.TestUtils.VALID_ID;
import static mate.academy.carsharing.util.TestUtils.VALID_RENTAL_DATE;
import static mate.academy.carsharing.util.TestUtils.VALID_RETURN_DATE;
import static mate.academy.carsharing.util.TestUtils.createExpiredPayments;
import static mate.academy.carsharing.util.TestUtils.createOverdueRental;
import static mate.academy.carsharing.util.TestUtils.createValidCar;
import static mate.academy.carsharing.util.TestUtils.createValidRental;
import static mate.academy.carsharing.util.TestUtils.createValidRentalRequestDto;
import static mate.academy.carsharing.util.TestUtils.createValidRentalResponseDto;
import static mate.academy.carsharing.util.TestUtils.createValidUser;
import static mate.academy.carsharing.util.TestUtils.returnedRentalResponseDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mate.academy.carsharing.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.dto.rental.RentalResponseDto;
import mate.academy.carsharing.dto.rental.RentalSearchParametersDto;
import mate.academy.carsharing.exception.RentalException;
import mate.academy.carsharing.mapper.RentalMapper;
import mate.academy.carsharing.model.Car;
import mate.academy.carsharing.model.Payment;
import mate.academy.carsharing.model.Rental;
import mate.academy.carsharing.model.User;
import mate.academy.carsharing.repository.SpecificationProvider;
import mate.academy.carsharing.repository.car.CarRepository;
import mate.academy.carsharing.repository.payment.PaymentRepository;
import mate.academy.carsharing.repository.rental.RentalRepository;
import mate.academy.carsharing.repository.rental.RentalSpecificationBuilder;
import mate.academy.carsharing.repository.rental.RentalSpecificationProviderManager;
import mate.academy.carsharing.repository.rental.spec.RentalIsActiveSpecificationProvider;
import mate.academy.carsharing.repository.rental.spec.RentalUserIdSpecificationProvider;
import mate.academy.carsharing.repository.user.UserRepository;
import mate.academy.carsharing.service.NotificationService;
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
public class RentalServiceTest {
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private RentalMapper rentalMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RentalSpecificationBuilder rentalSpecificationBuilder;
    private final ObjectMapper notMockedMapper = new ObjectMapper();
    @InjectMocks
    private RentalServiceImpl rentalService;

    {
        notMockedMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("save() method works")
    public void save_WithValidCreateRentalRequestDto_ReturnRental() {
        CreateRentalRequestDto requestDto = createValidRentalRequestDto();
        Car car = createValidCar();
        User user = createValidUser();
        Rental rental = createValidRental();
        RentalResponseDto expected = createValidRentalResponseDto();
        when(paymentRepository.getAllByUserIdAndPaymentStatus(user.getId(), Status.EXPIRED))
                .thenReturn(List.of());
        when(carRepository.findById(requestDto.carId()))
                .thenReturn(Optional.of(car));
        when(userRepository.findById(requestDto.userId()))
                .thenReturn(Optional.of(user));
        when(rentalMapper.toModel(requestDto))
                .thenReturn(rental);
        ObjectWriter objectWriter = notMockedMapper.writerWithDefaultPrettyPrinter();
        when(objectMapper.writerWithDefaultPrettyPrinter())
                .thenReturn(objectWriter);
        doNothing()
                .when(notificationService).sendNotification(anyLong(), anyString());
        when(rentalRepository.save(any(Rental.class)))
                .thenReturn(rental);
        when(rentalMapper.toDto(any(Rental.class)))
                .thenReturn(expected);

        RentalResponseDto actualResponse = rentalService.save(requestDto);

        assertEquals(expected, actualResponse);
    }

    @Test
    @DisplayName("save() method throws RentalException when user has expired payments")
    public void save_WithExpiredPayments_ThrowsRentalException() {
        CreateRentalRequestDto requestDto = createValidRentalRequestDto();
        List<Payment> expiredPayments = List.of(createExpiredPayments());
        when(paymentRepository.getAllByUserIdAndPaymentStatus(requestDto.userId(), Status.EXPIRED))
                .thenReturn(expiredPayments);

        Assertions.assertThrows(RentalException.class, () -> rentalService.save(requestDto));
    }

    @Test
    @DisplayName("save() method throws RentalException when there is no available car")
    public void save_WhenThereIsNoAvailableCar_ThrowsRentalException() {
        Car notAvailableCar = createValidCar();
        notAvailableCar.setInventory(0);

        when(carRepository.findById(anyLong()))
                .thenReturn(Optional.of(notAvailableCar));
        doNothing()
                .when(notificationService).sendNotification(anyLong(), anyString());

        CreateRentalRequestDto requestDto = createValidRentalRequestDto();

        Assertions.assertThrows(RentalException.class, () -> rentalService.save(requestDto));
    }

    @Test
    @DisplayName("searchRentals() method returns active rentals for specified users")
    public void searchRentals_WithValidParams_ReturnActiveRentals() {
        Rental notReturnedYetRental = createValidRental();

        String[] parameterToSearchForTheseUserIds = {VALID_ID.toString()};
        String[] parameterToSearchForActiveRentals = {"true"};
        RentalSearchParametersDto parametersDto = new RentalSearchParametersDto(
                parameterToSearchForTheseUserIds, parameterToSearchForActiveRentals);
        List<SpecificationProvider<Rental>> listOfSpecificationProviders =
                List.of(new RentalIsActiveSpecificationProvider(),
                        new RentalUserIdSpecificationProvider());
        RentalSpecificationProviderManager specificationProviderManager =
                new RentalSpecificationProviderManager(listOfSpecificationProviders);
        RentalSpecificationBuilder specificationBuilder =
                new RentalSpecificationBuilder(specificationProviderManager);
        Specification<Rental> specification = specificationBuilder.build(parametersDto);

        when(rentalSpecificationBuilder.build(any(RentalSearchParametersDto.class)))
                .thenReturn(specification);
        when(rentalRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<Rental>(List.of(notReturnedYetRental)));

        RentalResponseDto responseDtoWithNotReturnedYetRental = createValidRentalResponseDto();
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(responseDtoWithNotReturnedYetRental);

        List<RentalResponseDto> expected = List.of(responseDtoWithNotReturnedYetRental);
        Pageable pageable = PageRequest.of(0, 20);
        List<RentalResponseDto> actual = rentalService.searchRentals(parametersDto, pageable);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("searchRentals() method returns nonactive rentals for specified users")
    public void searchRentals_WithValidParams_ReturnNonactiveRentals() {
        Rental returnedRental = createValidRental();
        returnedRental.setActualReturnDate(VALID_ACTUAL_RETURN_DATE);
        RentalResponseDto responseDtoWithReturnedRental =
                new RentalResponseDto(
                        VALID_ID,
                        VALID_RENTAL_DATE,
                        VALID_RETURN_DATE,
                        VALID_ACTUAL_RETURN_DATE,
                        VALID_ID,
                        VALID_ID
                );

        String[] parameterToSearchForTheseUserIds = {VALID_ID.toString()};
        String[] parameterToSearchForNonactiveRentals = {"false"};
        RentalSearchParametersDto parametersDto = new RentalSearchParametersDto(
                parameterToSearchForTheseUserIds, parameterToSearchForNonactiveRentals);
        List<SpecificationProvider<Rental>> listOfSpecificationProviders =
                List.of(new RentalIsActiveSpecificationProvider(),
                        new RentalUserIdSpecificationProvider());
        RentalSpecificationProviderManager specificationProviderManager =
                new RentalSpecificationProviderManager(listOfSpecificationProviders);
        RentalSpecificationBuilder specificationBuilder =
                new RentalSpecificationBuilder(specificationProviderManager);
        Specification<Rental> specification = specificationBuilder.build(parametersDto);

        when(rentalSpecificationBuilder.build(any(RentalSearchParametersDto.class)))
                .thenReturn(specification);
        when(rentalRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<Rental>(List.of(returnedRental)));
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(responseDtoWithReturnedRental);

        List<RentalResponseDto> expected = List.of(responseDtoWithReturnedRental);
        Pageable pageable = PageRequest.of(0, 20);
        List<RentalResponseDto> actual = rentalService.searchRentals(parametersDto, pageable);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("searchRentals() method returns all rentals for specified users")
    public void searchRentals_WithValidParams_ReturnAllRentals() {
        Rental notReturnedYetRental = createValidRental();

        Rental returnedRental = createValidRental();
        returnedRental.setActualReturnDate(VALID_ACTUAL_RETURN_DATE);
        RentalResponseDto responseDtoWithReturnedRental =
                new RentalResponseDto(
                        VALID_ID,
                        VALID_RENTAL_DATE,
                        VALID_RETURN_DATE,
                        VALID_ACTUAL_RETURN_DATE,
                        VALID_ID,
                        VALID_ID
                );

        String[] parameterToSearchForTheseUserIds = {VALID_ID.toString()};
        String[] parameterToSearchForNonactiveRentals = {"false"};
        RentalSearchParametersDto parametersDto = new RentalSearchParametersDto(
                parameterToSearchForTheseUserIds, parameterToSearchForNonactiveRentals);
        List<SpecificationProvider<Rental>> listOfSpecificationProviders =
                List.of(new RentalIsActiveSpecificationProvider(),
                        new RentalUserIdSpecificationProvider());
        RentalSpecificationProviderManager specificationProviderManager =
                new RentalSpecificationProviderManager(listOfSpecificationProviders);
        RentalSpecificationBuilder specificationBuilder =
                new RentalSpecificationBuilder(specificationProviderManager);
        Specification<Rental> specification = specificationBuilder.build(parametersDto);

        when(rentalSpecificationBuilder.build(any(RentalSearchParametersDto.class)))
                .thenReturn(specification);
        when(rentalRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<Rental>(List.of(returnedRental, notReturnedYetRental)));

        RentalResponseDto responseDtoWithNotReturnedYetRental = createValidRentalResponseDto();
        when(rentalMapper.toDto(any(Rental.class)))
                .thenReturn(responseDtoWithReturnedRental, responseDtoWithNotReturnedYetRental);

        List<RentalResponseDto> expected =
                List.of(responseDtoWithReturnedRental, responseDtoWithNotReturnedYetRental);
        Pageable pageable = PageRequest.of(0, 20);
        List<RentalResponseDto> actual = rentalService.searchRentals(parametersDto, pageable);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("getRentalByIdAndUserEmail() method works")
    public void getRentalByIdAndUserEmail_ValidRentalIdAndUserEmail_ValidRentalResponseDto() {
        Rental rental = createValidRental();
        RentalResponseDto expected = createValidRentalResponseDto();
        when(userRepository.findByEmail(VALID_EMAIL))
                .thenReturn(Optional.of(createValidUser()));
        when(rentalRepository.findByIdAndUserId(VALID_ID, VALID_ID))
                .thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental))
                .thenReturn(expected);

        RentalResponseDto actual = rentalService.getRentalByIdAndUserEmail(VALID_ID, VALID_EMAIL);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("returnRental() method returns rental with specified id")
    public void returnRental_WithValidRentalId_ReturnRental() {
        Rental rentalFromDb = createValidRental();
        Rental returnedRental = createValidRental();
        returnedRental.setActualReturnDate(VALID_ACTUAL_RETURN_DATE);
        RentalResponseDto expected = returnedRentalResponseDto();

        when(rentalRepository.findById(VALID_ID))
                .thenReturn(Optional.of(rentalFromDb));
        when(rentalRepository.save(any(Rental.class)))
                .thenReturn(returnedRental);
        when(rentalMapper.toDto(returnedRental))
                .thenReturn(expected);

        ObjectWriter objectWriter = notMockedMapper.writerWithDefaultPrettyPrinter();
        when(objectMapper.writerWithDefaultPrettyPrinter())
                .thenReturn(objectWriter);
        doNothing()
                .when(notificationService).sendNotification(anyLong(), anyString());

        RentalResponseDto actual = rentalService.returnRental(VALID_ID);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("checkOverdueRentals() method sends notifications about every overdue rental")
    public void checkOverdueRentals_WithOverdueRentals() {
        List<Rental> overdueRentals = List.of(createOverdueRental(), createOverdueRental());
        when(rentalRepository
                .findAllByReturnDateBeforeAndActualReturnDateIsNull(any(LocalDate.class)))
                .thenReturn(overdueRentals);
        doNothing()
                .when(notificationService).sendNotification(anyLong(), anyString());

        rentalService.checkOverdueRentals();

        verify(notificationService, times(overdueRentals.size()))
                .sendNotification(anyLong(), anyString());
    }

    @Test
    @DisplayName("checkOverdueRentals() method sends global notification "
            + "when no rentals are overdue")
    public void checkOverdueRentals_NoOverdueRentals() {
        when(rentalRepository.findAllByReturnDateBeforeAndActualReturnDateIsNull(
                any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        doNothing()
                .when(notificationService).sendGlobalNotification(anyString());

        rentalService.checkOverdueRentals();

        verify(notificationService, times(1))
                .sendGlobalNotification(anyString());
    }
}
