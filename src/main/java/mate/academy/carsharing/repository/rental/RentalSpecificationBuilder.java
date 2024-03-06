package mate.academy.carsharing.repository.rental;

import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.dto.rental.RentalSearchParametersDto;
import mate.academy.carsharing.model.Rental;
import mate.academy.carsharing.repository.SpecificationBuilder;
import mate.academy.carsharing.repository.SpecificationProviderManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RentalSpecificationBuilder
        implements SpecificationBuilder<Rental, RentalSearchParametersDto> {
    private final SpecificationProviderManager<Rental> rentalSpecificationProviderManager;

    @Override
    public Specification<Rental> build(RentalSearchParametersDto searchParameters) {
        Specification<Rental> specification = Specification.where(null);
        if (searchParameters.user_id() != null && searchParameters.user_id().length > 0) {
            specification = specification
                    .and(rentalSpecificationProviderManager.getSpecificationProvider("userId")
                            .getSpecification(searchParameters.user_id()));
        }
        if (searchParameters.is_active() != null && searchParameters.is_active().length > 0) {
            specification = specification
                    .and(rentalSpecificationProviderManager.getSpecificationProvider("isActive")
                            .getSpecification(searchParameters.is_active()));
        }
        return specification;
    }
}
