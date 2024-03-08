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
import mate.academy.carsharing.model.Rental;
import mate.academy.carsharing.model.User;
import mate.academy.carsharing.repository.car.CarRepository;
import mate.academy.carsharing.repository.rental.RentalRepository;
import mate.academy.carsharing.repository.rental.RentalSpecificationBuilder;
import mate.academy.carsharing.repository.user.UserRepository;
import mate.academy.carsharing.service.NotificationService;
import mate.academy.carsharing.service.RentalService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RentalServiceImpl implements RentalService {
    private static final Integer MIN_REQUIRED_CAR_AVAILABLE = 1;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final RentalMapper rentalMapper;
    private final RentalSpecificationBuilder rentalSpecificationBuilder;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RentalResponseDto save(CreateRentalRequestDto requestDto) {
        Car car = getCarById(requestDto.carId());
        int inventory = car.getInventory();
        if (inventory < MIN_REQUIRED_CAR_AVAILABLE) {
            throw new RentalException("There is no car available with id: " + requestDto.carId());
        }
        car.setInventory(inventory - 1);
        Rental newRental = rentalMapper.toModel(requestDto);
        newRental.setCar(car);
        newRental.setUser(getUserById(requestDto.userId()));
        Rental savedRental = rentalRepository.save(newRental);
        RentalResponseDto savedRentalDto = rentalMapper.toDto(savedRental);
        notifyUser("Your rental created!\\n", savedRentalDto);
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
        notifyUser("you have just returned the rental!\\n", savedRentalDto);
        return savedRentalDto;
    }

    private void notifyUser(String message, RentalResponseDto savedRentalDto) {
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
}
