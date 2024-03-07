package mate.academy.carsharing.service;

import java.util.List;
import mate.academy.carsharing.dto.rental.CreateRentalRequestDto;
import mate.academy.carsharing.dto.rental.RentalResponseDto;
import mate.academy.carsharing.dto.rental.RentalSearchParametersDto;
import org.springframework.data.domain.Pageable;

public interface RentalService {
    RentalResponseDto save(CreateRentalRequestDto requestDto);

    List<RentalResponseDto> searchRentals(
            RentalSearchParametersDto searchParameters, Pageable pageable);

    RentalResponseDto getRentalByIdAndUserEmail(Long id, String email);

    RentalResponseDto returnRental(Long id);
}
