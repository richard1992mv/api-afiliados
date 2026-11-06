package com.compensar.afiliados.infrastructure.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuración de los JWT que acepta la API.
 *
 * @param secreto llave HMAC en Base64 (variable JWT_SECRET); nunca se escribe en el código ni en los logs
 * @param emisor  valor esperado del claim "iss"
 */
@Validated
@ConfigurationProperties("afiliados.seguridad.jwt")
public record JwtProperties(@NotBlank String secreto, @NotBlank String emisor) {

	/** Oculta el secreto si el record llega a imprimirse (logs, actuator, depuración). */
	@Override
	public String toString() {
		return "JwtProperties[secreto=****, emisor=" + emisor + "]";
	}
}
