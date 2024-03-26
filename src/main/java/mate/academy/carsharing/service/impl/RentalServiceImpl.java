package mate.academy.carsharing.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.dto.rental.RentalResponseDto;
import mate.academy.carsharing.dto.rental.RentalSearchParametersDto;
import mate.academy.carsharing.exception.EntityNotFoundException;
import mate.academy.carsharing.exception.RentalException;
import mate.academy.carsharing.mapper.RentalMapper;
import mate.academy.carsharing.model.Car;
import mate.academy.carsharing.model.Payment;
import mate.academy.carsharing.model.Rental;
import mate.academy.carsharing.model.User;
import mate.academy.carsharing.repository.car.CarRepository;
import mate.academy.carsharing.repository.payment.PaymentRepository;
import mate.academy.carsharing.repository.rental.RentalRepository;
import mate.academy.carsharing.repository.rental.RentalSpecificationBuilder;
import mate.academy.carsharing.repository.user.UserRepository;
import mate.academy.carsharing.service.NotificationService;
import mate.academy.carsharing.service.RentalService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RentalServiceImpl implements RentalService {
    private static final Integer MIN_REQUIRED_CAR_AVAILABLE = 1;
    private static final String THERE_IS_NO_CAR_AVAILABLE_WITH_ID =
            "There is no car available with id: ";
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final PaymentRepository paymentRepository;
    private final RentalMapper rentalMapper;
    private final RentalSpecificationBuilder rentalSpecificationBuilder;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RentalResponseDto save(CreateRentalRequestDto requestDto) {
        if (!isAllowedToRentCar(requestDto.userId())) {
            throw new RentalException("User can't rent a car. There are expired payments.");
        }
        Car car = getCarById(requestDto.carId());
        checkIsCarAvailable(requestDto, car);
        car.setInventory(car.getInventory() - 1);
        Rental newRental = rentalMapper.toModel(requestDto);
        newRental.setCar(car);
        newRental.setUser(getUserById(requestDto.userId()));
        Rental savedRental = rentalRepository.save(newRental);
        RentalResponseDto savedRentalDto = rentalMapper.toDto(savedRental);
        notifyUserWithRentalInfo("Your rental created!\\n", savedRentalDto);
        return savedRentalDto;
    }

    @Override
    public RentalResponseDto getRentalByIdAndUserEmail(Long id, String email) {
        Rental rental = getRentalByIdAndUserId(id, getUserByEmail(email).getId());
        return rentalMapper.toDto(rental);
    }

    @Override
    public List<RentalResponseDto> searchRentals(
            RentalSearchParametersDto searchParameters, Pageable pageable) {
        Specification<Rental> rentalSpecification =
                rentalSpecificationBuilder.build(searchParameters);
        return rentalRepository.findAll(rentalSpecification, pageable)
                .stream()
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public RentalResponseDto returnRental(Long id) {
        Rental rental = getRentalById(id);
        if (Objects.nonNull(rental.getActualReturnDate())) {
            throw new RentalException("Rental with id: " + id + " already returned.");
        }
        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);
        rental.setActualReturnDate(LocalDate.now());
        Rental savedRental = rentalRepository.save(rental);
        RentalResponseDto savedRentalDto = rentalMapper.toDto(savedRental);
        notifyUserWithRentalInfo("you have just returned the rental!\\n", savedRentalDto);
        return savedRentalDto;
    }

    @Scheduled(cron = "0 0 10 * * *")
    public void checkOverdueRentals() {
        LocalDate today = LocalDate.now();
        List<Rental> overdueRentals =
                rentalRepository.findAllByReturnDateBeforeAndActualReturnDateIsNull(today);
        if (overdueRentals.isEmpty()) {
            notificationService.sendGlobalNotification("No rentals overdue today!");
            return;
        }
        for (Rental rental : overdueRentals) {
            String message = createOverdueRentalMessage(rental);
            notificationService.sendNotification(rental.getUser().getId(), message);
        }
    }

    private void checkIsCarAvailable(CreateRentalRequestDto requestDto, Car car) {
        if (car.getInventory() < MIN_REQUIRED_CAR_AVAILABLE) {
            notificationService.sendNotification(requestDto.userId(),
                    THERE_IS_NO_CAR_AVAILABLE_WITH_ID + requestDto.carId());
            throw new RentalException(THERE_IS_NO_CAR_AVAILABLE_WITH_ID + requestDto.carId());
        }
    }

    private String createOverdueRentalMessage(Rental rental) {
        return "Overdue rental alert! Rental ID: " + rental.getId()
                + ", User ID: " + rental.getUser().getId()
                + ", Car ID: " + rental.getCar().getId()
                + ", Return Date: " + rental.getReturnDate();
    }

    private void notifyUserWithRentalInfo(String message, RentalResponseDto savedRentalDto) {
        String stringWithRentalDto;
        try {
            stringWithRentalDto = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(savedRentalDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        notificationService.sendNotification(savedRentalDto.userId(),
                message + stringWithRentalDto);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Can't find user with email: " + email)
        );
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id: " + userId)
        );
    }

    private Rental getRentalByIdAndUserId(Long rentalId, Long userId) {
        return rentalRepository
                .findByIdAndUserId(rentalId, userId).orElseThrow(
                        () -> new EntityNotFoundException("Can't find rental with id: " + rentalId)
                );
    }

    private Rental getRentalById(Long rentalId) {
        return rentalRepository
                .findById(rentalId).orElseThrow(
                        () -> new EntityNotFoundException("Can't find rental with id: " + rentalId)
                );
    }

    private Car getCarById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find car by id: " + id));
    }

    private boolean isAllowedToRentCar(Long userId) {
        List<Payment> expiredPayments = paymentRepository.getAllByUserIdAndPaymentStatus(userId,
                Payment.Status.EXPIRED);
        return expiredPayments.isEmpty();
    }
}
