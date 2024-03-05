package mate.academy.carsharing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.dto.rental.RentalResponseDto;
import mate.academy.carsharing.service.RentalService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rental managing", description = "Endpoint to rentals managing")
@RequiredArgsConstructor
@RestController
@RequestMapping("api/rentals")
public class RentalController {
    private final RentalService rentalService;

    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping()
    @Operation(summary = "Create a new rental", description = "Create a new rental")
    public RentalResponseDto create(@RequestBody @Valid CreateRentalRequestDto requestDto) {
        return rentalService.save(requestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/{id}")
    @Operation(summary = "Get rental by id", description = "Get rental by id")
    public RentalResponseDto getRental(@PathVariable Long id, Authentication authentication) {
        return rentalService.getRentalById(id, authentication.getName());
    }
}