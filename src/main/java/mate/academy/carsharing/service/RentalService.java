package mate.academy.carsharing.service;

import java.util.List;
import mate.academy.carsharing.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.dto.rental.RentalResponseDto;

public interface RentalService {
    RentalResponseDto save(CreateRentalRequestDto requestDto);

    List<RentalResponseDto> getRentalsByUserIdAndRentalState(Long userId, boolean isRentalActive);

    RentalResponseDto getRentalById(Long rentalId, String email);

    RentalResponseDto returnRental(Long rentalId, String email);
}
