package mate.academy.carsharing.repository.rental.spec;

import jakarta.persistence.criteria.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import mate.academy.carsharing.model.Rental;
import mate.academy.carsharing.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class RentalIsActiveSpecificationProvider implements SpecificationProvider<Rental> {
    @Override
    public String getKey() {
        return "isActive";
    }

    public Specification<Rental> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) -> {
            Path<Integer> idPath = root.get("id");
            Path<LocalDate> actualReturnDatePath = root.get("actualReturnDate");
            AtomicInteger trueCount = new AtomicInteger();
            AtomicInteger falseCount = new AtomicInteger();
            Arrays.stream(params)
                    .forEach(str -> {
                        if ("true".equals(str)) {
                            trueCount.getAndIncrement();
                        } else if ("false".equals(str)) {
                            falseCount.getAndIncrement();
                        }
                    });
            if (trueCount.get() > falseCount.get()) {
                return actualReturnDatePath.isNull();
            } else if (trueCount.get() < falseCount.get()) {
                return actualReturnDatePath.isNotNull();
            } else {
                return idPath.isNotNull();
            }
        };
    }
}
