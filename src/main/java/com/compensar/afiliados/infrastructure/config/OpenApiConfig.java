package com.compensar.afiliados.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos de la documentación OpenAPI, publicada en /swagger-ui.html.
 * Declara el esquema Bearer JWT: Swagger UI muestra el botón "Authorize" para pegar el token.
 */
@Configuration
public class OpenApiConfig {

	static final String ESQUEMA_JWT = "bearer-jwt";

	@Bean
	OpenAPI afiliadosOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("API Afiliados")
						.version("v1")
						.description("Gestión de afiliados de la caja de compensación. "
								+ "Requiere un JWT (Bearer) con el claim 'roles': ADMIN puede todo; CONSULTA solo GET. "
								+ "Los errores siguen el formato ProblemDetail (RFC 9457)."))
				.components(new Components().addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.description("Pegue solo el token, sin el prefijo 'Bearer '.")))
				// Aplica el esquema a todas las operaciones (las públicas lo anulan con @SecurityRequirements vacío).
				.addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
	}
}
