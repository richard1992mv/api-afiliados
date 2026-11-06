package com.compensar.afiliados.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos de la documentación OpenAPI, publicada en /swagger-ui.html.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI afiliadosOpenApi() {
		return new OpenAPI().info(new Info()
				.title("API Afiliados")
				.version("v1")
				.description("Gestión de afiliados de la caja de compensación. "
						+ "Los errores siguen el formato ProblemDetail (RFC 7807)."));
	}
}
