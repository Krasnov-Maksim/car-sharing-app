package mate.academy.carsharing.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import mate.academy.carsharing.model.Car;

@Data()
@NoArgsConstructor
public class CarResponseDto {
    private Long id;
    private String model;
    private String brand;
    private Car.Type type;
    private Integer inventory;
    private BigDecimal dailyFee;
}
