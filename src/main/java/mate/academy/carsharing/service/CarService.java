package mate.academy.carsharing.service;

import java.util.List;
import mate.academy.carsharing.dto.car.CarResponseDto;
import mate.academy.carsharing.dto.car.CreateCarRequestDto;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarResponseDto save(CreateCarRequestDto requestDto);

    void deleteById(Long id);

    CarResponseDto updateById(Long id, CreateCarRequestDto requestDto);

    CarResponseDto getById(Long id);

    List<CarResponseDto> getAll(Pageable pageable);
}
