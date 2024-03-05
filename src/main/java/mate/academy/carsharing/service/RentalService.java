package mate.academy.carsharing.service;

import java.util.List;
import mate.academy.carsharing.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.dto.rental.RentalResponseDto;

public interface RentalService {
    RentalResponseDto addNewRental(CreateRentalRequestDto requestDto);

    List<RentalResponseDto> getAllCurrentRentals(Long userId, boolean isActive);

    RentalResponseDto getRentalById(Long id, String email);

    RentalResponseDto returnRental(Long rentalId, String email);
}
