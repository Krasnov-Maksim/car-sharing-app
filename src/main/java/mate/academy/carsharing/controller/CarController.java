package mate.academy.carsharing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.dto.CarResponseDto;
import mate.academy.carsharing.dto.CreateCarRequestDto;
import mate.academy.carsharing.service.CarService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/cars")
@Tag(name = "Car management", description = "Cars Management Endpoints")
public class CarController {
    private final CarService carService;

    @Operation(summary = "Create a new car", description = "Create a new car")
    @PostMapping
    public CarResponseDto create(@RequestBody @Valid CreateCarRequestDto carDto) {
        return carService.save(carDto);
    }

    @Operation(summary = "Delete a car by id", description = "Delete car with specified id")
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        carService.deleteById(id);
    }

    @Operation(summary = "Update a car by id", description = "Update car with specified id")
    @PutMapping("/{id}")
    public CarResponseDto updateById(@PathVariable Long id,
                                     @RequestBody @Valid CreateCarRequestDto requestDto) {
        return carService.updateById(id, requestDto);
    }

    @Operation(summary = "Get a car by id", description = "Get a car with specified id")
    @GetMapping("/{id}")
    public CarResponseDto getById(@PathVariable Long id) {
        return carService.getById(id);
    }

    @Operation(summary = "Get all cars", description = "Get a list of all cars")
    @Parameter(name = "page", description = "page index, default value = 0")
    @Parameter(name = "size", description = "elements per page, default value = 20")
    @Parameter(name = "sort", description = "sort criteria", example = "title,Desc")
    @GetMapping
    public List<CarResponseDto> getAll(Pageable pageable) {
        return carService.getAll(pageable);
    }
}
