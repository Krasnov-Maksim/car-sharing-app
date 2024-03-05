package mate.academy.carsharing.repository.rental;

import java.util.List;
import java.util.Optional;
import mate.academy.carsharing.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    Optional<Rental> findByIdAndUserId(Long rentalId, Long userId);

    List<Rental> findAllByUserIdAndActualReturnDateNotNull(Long userId);

    List<Rental> findAllByUserIdAndActualReturnDateIsNull(Long userId);
}
