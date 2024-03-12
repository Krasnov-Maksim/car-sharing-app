package mate.academy.carsharing.repository.payment.spec;

import jakarta.persistence.criteria.Path;
import java.util.Arrays;
import mate.academy.carsharing.model.Payment;
import mate.academy.carsharing.model.User;
import mate.academy.carsharing.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class PaymentUserIdSpecificationProvider implements SpecificationProvider<Payment> {
    @Override
    public String getKey() {
        return "userId";
    }

    public Specification<Payment> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) -> {
            Path<User> userPath = root.get("user");
            Path<Long> idPath = userPath.get("id");
            return idPath.in(Arrays.stream(params).toArray());
        };
    }
}
