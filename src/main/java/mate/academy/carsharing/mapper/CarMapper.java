package mate.academy.carsharing.mapper;

import mate.academy.carsharing.config.MapperConfig;
import mate.academy.carsharing.dto.CarResponseDto;
import mate.academy.carsharing.dto.CreateCarRequestDto;
import mate.academy.carsharing.model.Car;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface CarMapper {

    CarResponseDto toDto(Car car);

    Car toModel(CreateCarRequestDto requestDto);
}
