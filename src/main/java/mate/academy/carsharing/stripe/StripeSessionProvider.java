package mate.academy.carsharing.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StripeSessionProvider {
    private static final String DEFAULT_CURRENCY = "usd";
    @Value("${STRIPE_SUCCESS_LINK}")
    private String successUrl;
    @Value("${STRIPE_CANCEL_LINK}")
    private String cancelUrl;

    public Session createStripeSession(BigDecimal amount, String productName) throws StripeException {
        PriceData priceData = PriceData.builder()
                .setCurrency(DEFAULT_CURRENCY)
                .setUnitAmountDecimal(amount)
                .setProductData(PriceData.ProductData.builder()
                        .setName(productName)
                        .build())
                .build();
        SessionCreateParams params = SessionCreateParams.builder()
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(priceData)
                        .build())
                .build();
        return Session.create(params);
    }

    public Session retrieveSession(String sessionId) throws StripeException {
        return Session.retrieve(sessionId);
    }
}