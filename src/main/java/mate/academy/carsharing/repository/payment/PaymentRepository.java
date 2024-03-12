package mate.academy.carsharing.repository.payment;

import mate.academy.carsharing.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findAll(Specification<Payment> spec, Pageable pageable);

}
