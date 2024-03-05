package mate.academy.carsharing.repository.rental;

import mate.academy.carsharing.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {
}