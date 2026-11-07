package com.compensar.afiliados.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Cuerpo de PUT /api/v1/afiliados/{id}. El documento no se actualiza: es la identidad del afiliado.
 */
@Schema(description = "Datos para actualizar un afiliado activo")
public record ActualizarAfiliadoRequest(

		@Schema(example = "Ana María")
		@NotBlank(message = "Los nombres son obligatorios")
		@Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
		String nombres,

		@Schema(example = "Pérez Ruiz")
		@NotBlank(message = "Los apellidos son obligatorios")
		@Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
		String apellidos,

		@Schema(example = "ana.perez@correo.com")
		@NotBlank(message = "El email es obligatorio")
		@Email(message = "El email no tiene un formato válido")
		@Size(max = 254, message = "El email no puede superar 254 caracteres")
		String email,

		@Schema(description = "Salario mensual en pesos", example = "3500000")
		@NotNull(message = "El salario es obligatorio")
		@Positive(message = "El salario debe ser mayor que cero")
		@Digits(integer = 13, fraction = 2, message = "El salario admite hasta 13 enteros y 2 decimales")
		BigDecimal salario) {
}
