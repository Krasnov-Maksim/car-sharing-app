package mate.academy.carsharing.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Car-Sharing-App REST API", version = "1.0",
        description = "REST API for Car-Sharing-App. Register, Login and use JWT token."),
        security = {@SecurityRequirement(name = "bearerToken")}
)
@SecuritySchemes({@SecurityScheme(name = "bearerToken", type = SecuritySchemeType.HTTP,
        scheme = "bearer", bearerFormat = "JWT")}
)
public class OpenApiConfig {
}
