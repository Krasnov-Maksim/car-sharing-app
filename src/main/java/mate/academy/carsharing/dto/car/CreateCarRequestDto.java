package mate.academy.carsharing.dto.car;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mate.academy.carsharing.model.Car;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder()
public class CreateCarRequestDto {
    @NotBlank(message = "Model cannot be blank")
    private String model;
    @NotBlank(message = "Brand cannot be blank")
    private String brand;
    private Car.Type type;
    @PositiveOrZero
    private Integer inventory;
    @PositiveOrZero
    private BigDecimal dailyFee;
}
