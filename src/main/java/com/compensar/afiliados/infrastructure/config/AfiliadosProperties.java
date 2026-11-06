package com.compensar.afiliados.infrastructure.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * Propiedades de negocio bajo el prefijo {@code afiliados}. Se validan al arrancar:
 * si el SMMLV falta o no es positivo, la aplicación no inicia (fail fast).
 */
@Validated
@ConfigurationProperties(prefix = "afiliados")
public record AfiliadosProperties(

		/** RN2: salario mínimo mensual legal vigente. */
		@NotNull @Positive BigDecimal smmlv) {
}
