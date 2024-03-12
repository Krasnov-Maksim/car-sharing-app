package mate.academy.carsharing.repository.payment;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.model.Payment;
import mate.academy.carsharing.repository.SpecificationProvider;
import mate.academy.carsharing.repository.SpecificationProviderManager;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentSpecificationProviderManager implements SpecificationProviderManager<Payment> {
    private final List<SpecificationProvider<Payment>> paymentSpecificationProviders;

    @Override
    public SpecificationProvider<Payment> getSpecificationProvider(String key) {
        return paymentSpecificationProviders.stream()
                .filter(provider -> provider.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Can't find correct specification provider for key " + key));
    }
}
