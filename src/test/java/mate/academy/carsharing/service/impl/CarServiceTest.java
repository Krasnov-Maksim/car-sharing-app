package mate.academy.carsharing.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import mate.academy.carsharing.dto.car.CarResponseDto;
import mate.academy.carsharing.dto.car.CreateCarRequestDto;
import mate.academy.carsharing.exception.EntityNotFoundException;
import mate.academy.carsharing.mapper.CarMapper;
import mate.academy.carsharing.model.Car;
import mate.academy.carsharing.repository.car.CarRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {
    private static final Long VALID_ID = 1L;
    private static final Long NOT_VALID_ID = -1L;
    private static final String VALID_MODEL = "Valid Model";
    private static final String VALID_BRAND = "Valid Brand";
    private static final Car.Type VALID_TYPE = Car.Type.SUV;
    private static final Integer VALID_INVENTORY = 10;
    private static final BigDecimal VALID_DAILY_FEE = BigDecimal.TEN;
    private static final boolean NOT_DELETED = false;
    @Mock
    private CarRepository carRepository;
    @Mock
    private CarMapper carMapper;
    @InjectMocks
    private CarServiceImpl carService;

    private Car createValidCar() {
        Car car = new Car();
        car.setId(VALID_ID);
        car.setModel(VALID_MODEL);
        car.setBrand(VALID_BRAND);
        car.setType(VALID_TYPE);
        car.setInventory(VALID_INVENTORY);
        car.setDailyFee(VALID_DAILY_FEE);
        return car;
    }

    private CreateCarRequestDto createValidCarRequestDto() {
        return new CreateCarRequestDto(
                VALID_MODEL,
                VALID_BRAND,
                VALID_TYPE,
                VALID_INVENTORY,
                VALID_DAILY_FEE
        );
    }

    private CarResponseDto createValidCarResponseDto() {
        CarResponseDto carResponseDto = new CarResponseDto();
        carResponseDto.setId(VALID_ID);
        carResponseDto.setModel(VALID_MODEL);
        carResponseDto.setBrand(VALID_BRAND);
        carResponseDto.setType(VALID_TYPE);
        carResponseDto.setInventory(VALID_INVENTORY);
        carResponseDto.setDailyFee(VALID_DAILY_FEE);
        return carResponseDto;
    }

    @Test
    @DisplayName("save() method works")
    public void save_WithValidCarRequestDto_ReturnCarResponseDto() {
        CreateCarRequestDto requestDto = createValidCarRequestDto();
        CarResponseDto expected = createValidCarResponseDto();
        Car car = createValidCar();
        when(carMapper.toModel(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(expected);

        CarResponseDto actual = carService.save(requestDto);

        assertEquals(actual, expected);
    }

    @Test
    @DisplayName("getAll() method works")
    public void getAll_WithValidPageable_ReturnCarResponseDtoList() {
        Car car = createValidCar();
        CarResponseDto responseDto = createValidCarResponseDto();
        List<CarResponseDto> expected = List.of(responseDto);
        Pageable pageable = PageRequest.of(0, 10);

        when(carRepository.findAll(pageable))
                .thenReturn(new PageImpl<Car>(List.of(car)));
        when(carMapper.toDto(car)).thenReturn(responseDto);

        List<CarResponseDto> actual = carService.getAll(pageable);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("getById() method works")
    public void getById_WithValidId_ReturnCarResponseDto() {
        Car car = createValidCar();
        CarResponseDto expected = createValidCarResponseDto();
        when(carRepository.findById(VALID_ID))
                .thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(expected);

        CarResponseDto actual = carService.getById(VALID_ID);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("getById() method with invalid 'id' throws EntityNotFoundException")
    public void getById_WithInvalidID_ThrowsEntityNotFoundException() {
        when(carRepository.findById(NOT_VALID_ID))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> carService.getById(NOT_VALID_ID));
    }

    @Test
    @DisplayName("updateById() method works")
    public void updateById_WithValidIdAndRequestDto_ReturnCarResponseDto() {
        Car car = createValidCar();
        CreateCarRequestDto requestDto = createValidCarRequestDto();
        CarResponseDto expected = createValidCarResponseDto();
        when(carRepository.findById(VALID_ID)).thenReturn(Optional.of(car));
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toModel(requestDto)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(expected);

        CarResponseDto actual = carService.updateById(VALID_ID, requestDto);

        assertEquals(expected, actual);
        verify(carMapper).toModel(requestDto);
        verify(carRepository).save(car);
    }

    @Test
    @DisplayName("updateById() method with invalid 'id' throws EntityNotFoundException")
    public void updateById_WithInvalidId_ThrowsEntityNotFoundException() {
        CreateCarRequestDto requestDto = createValidCarRequestDto();

        when(carRepository.findById(NOT_VALID_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> carService.updateById(NOT_VALID_ID, requestDto));
    }

    @Test
    @DisplayName("deleteById() method works")
    public void deleteById_WithValidId_ReturnCarResponseDto() {
        carService.deleteById(VALID_ID);
        verify(carRepository).deleteById(VALID_ID);
    }
}
