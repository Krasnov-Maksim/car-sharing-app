package mate.academy.carsharing.repository.payment;

import mate.academy.carsharing.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}