package mate.academy.carsharing.repository.payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import mate.academy.carsharing.model.Payment;
import mate.academy.carsharing.model.Rental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findAll(Specification<Payment> spec, Pageable pageable);

    @Query("SELECT SUM(p.amountToPay) FROM Payment p "
            + "WHERE p.rental = :rental AND p.status = :paymentStatus")
    BigDecimal getSumByRentalAndPaymentStatus(Rental rental, Payment.Status paymentStatus);

    Optional<Payment> findBySessionId(String sessionId);

    List<Payment> findAllByStatus(Payment.Status status);

    @Query("SELECT p FROM Payment p WHERE p.status = :paymentStatus AND p.rental.user.id = :userId")
    List<Payment> getAllByUserIdAndPaymentStatus(Long userId, Payment.Status paymentStatus);
}
