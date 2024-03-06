package mate.academy.carsharing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.dto.rental.RentalResponseDto;
import mate.academy.carsharing.dto.rental.RentalSearchParametersDto;
import mate.academy.carsharing.service.RentalService;
import org.springframework.data.domain.Pageable;
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
    @Operation(summary = "Create a new rental", description = "Admin creates a new rental")
    @PostMapping()
    public RentalResponseDto create(@RequestBody @Valid CreateRentalRequestDto requestDto) {
        return rentalService.save(requestDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Get list of rentals", description = "Admin can get list of "
            + "active/nonactive rentals for any number of specified users or for all users")
    @Parameter(name = "user_id", description = "Get rentals for users with specified 'user_id'."
            + "To get rentals for all users do not specify any value.", example = "2, 57")
    @Parameter(name = "is_active", description = "Specify 'true' to get active rentals or "
            + "'false' to get nonactive rentals. To get all rentals (active and nonactive) do not "
            + "specify any value.", example = "true")
    @GetMapping("/search")
    public List<RentalResponseDto> searchRentals(
            RentalSearchParametersDto searchParameters, Pageable pageable) {
        return rentalService.searchRentals(searchParameters, pageable);
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @Operation(summary = "Get rental info by id", description = "Get rental info by id")
    @GetMapping("/{id}")
    public RentalResponseDto getRental(@PathVariable Long id, Authentication authentication) {
        return rentalService.getRentalById(id, authentication.getName());
    }

    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Return rental", description = "Admin can return rental by id")
    @PostMapping("/{id}/return")
    public RentalResponseDto returnRental(@PathVariable Long id, Authentication authentication) {
        return rentalService.returnRental(id, authentication.getName());
    }
}
