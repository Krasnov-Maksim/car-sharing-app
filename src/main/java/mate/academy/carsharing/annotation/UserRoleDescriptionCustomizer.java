package mate.academy.carsharing.annotation;

import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class UserRoleDescriptionCustomizer implements OperationCustomizer {
    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        UserRoleDescription userRoleAnnotation =
                handlerMethod.getMethodAnnotation(UserRoleDescription.class);
        if (userRoleAnnotation != null) {
            PreAuthorize preAuthorizeAnnotation =
                    handlerMethod.getMethodAnnotation(PreAuthorize.class);
            if (preAuthorizeAnnotation != null
                    && preAuthorizeAnnotation.value().contains("ROLE_")) {
                String description = operation.getDescription() == null
                        ? "" : (operation.getDescription()/* + "\n"*/);
                int firstBraceIndex = preAuthorizeAnnotation.value().indexOf('(');
                int lastBraceIndex = preAuthorizeAnnotation.value().lastIndexOf(')');
                String rolesString = preAuthorizeAnnotation.value()
                        .substring(firstBraceIndex + 1, lastBraceIndex);
                operation.setDescription(description + " Required roles: " + rolesString + '.');
            }
        }
        return operation;
    }
}
