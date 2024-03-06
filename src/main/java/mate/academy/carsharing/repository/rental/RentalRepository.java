package mate.academy.carsharing.repository.rental;

import java.util.Optional;
import mate.academy.carsharing.model.Rental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    Optional<Rental> findByIdAndUserId(Long rentalId, Long userId);

    Page<Rental> findAll(Specification<Rental> spec, Pageable pageable);
}
