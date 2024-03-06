package mate.academy.carsharing.repository.rental.spec;

import jakarta.persistence.criteria.Path;
import java.util.Arrays;
import mate.academy.carsharing.model.Rental;
import mate.academy.carsharing.model.User;
import mate.academy.carsharing.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class RentalUserIdSpecificationProvider implements SpecificationProvider<Rental> {
    @Override
    public String getKey() {
        return "userId";
    }

    public Specification<Rental> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) -> {
            Path<User> userPath = root.get("user");
            Path<Long> idPath = userPath.get("id");
            return idPath.in(Arrays.stream(params).toArray());
        };
    }
}
