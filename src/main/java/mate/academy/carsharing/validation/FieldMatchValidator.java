package mate.academy.carsharing.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Objects;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
    private String firstFieldName;
    private String secondFieldName;

    @Override
    public void initialize(final FieldMatch constraintAnnotation) {
        this.firstFieldName = constraintAnnotation.firstField();
        this.secondFieldName = constraintAnnotation.secondField();
    }

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        try {
            Field firstField = value.getClass().getDeclaredField(firstFieldName);
            firstField.setAccessible(true);
            final Object firstValue = firstField.get(value);
            Field secondField = value.getClass().getDeclaredField(secondFieldName);
            secondField.setAccessible(true);
            final Object secondValue = secondField.get(value);
            return Objects.equals(firstValue, secondValue);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
