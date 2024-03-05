package mate.academy.carsharing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
    @Operation(summary = "Create a new rental", description = "Manager creates a new rental")
    @PostMapping()
    public RentalResponseDto create(@RequestBody @Valid CreateRentalRequestDto requestDto) {
        return rentalService.save(requestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Get list of rentals",
            description = "Get list of rentals for specified user and specified rental state")
    @GetMapping("/{userId}/{isActive}")
    public List<RentalResponseDto> getRentalsByUserIdAndRentalState(@PathVariable Long userId,
            @PathVariable boolean isActive) {
        return rentalService.getRentalsByUserIdAndRentalState(userId, isActive);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @Operation(summary = "Get rental info by id", description = "Get rental info by id")
    @GetMapping("/{id}")
    public RentalResponseDto getRental(@PathVariable Long id, Authentication authentication) {
        return rentalService.getRentalById(id, authentication.getName());
    }
}
