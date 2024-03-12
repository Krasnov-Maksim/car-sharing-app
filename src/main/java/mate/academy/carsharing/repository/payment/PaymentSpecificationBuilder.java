package mate.academy.carsharing.repository.payment;

import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.dto.payment.PaymentSearchParametersDto;
import mate.academy.carsharing.model.Payment;
import mate.academy.carsharing.repository.SpecificationBuilder;
import mate.academy.carsharing.repository.SpecificationProviderManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentSpecificationBuilder
        implements SpecificationBuilder<Payment, PaymentSearchParametersDto> {
    private final SpecificationProviderManager<Payment> paymentSpecificationProviderManager;

    @Override
    public Specification<Payment> build(PaymentSearchParametersDto searchParameters) {
        Specification<Payment> specification = Specification.where(null);
        if (searchParameters.user_id() != null && searchParameters.user_id().length > 0) {
            specification = specification
                    .and(paymentSpecificationProviderManager.getSpecificationProvider("userId")
                            .getSpecification(searchParameters.user_id()));
        }
        return specification;
    }
}
