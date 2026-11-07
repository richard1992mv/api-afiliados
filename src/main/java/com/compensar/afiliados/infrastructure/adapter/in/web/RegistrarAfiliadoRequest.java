package com.compensar.afiliados.infrastructure.adapter.in.web;

import com.compensar.afiliados.domain.model.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Cuerpo de POST /api/v1/afiliados. Valida la forma de la entrada; las reglas de negocio
 * se vuelven a validar en el dominio (defensa en profundidad).
 */
@Schema(description = "Datos para registrar un afiliado")
public record RegistrarAfiliadoRequest(

		@Schema(description = "RN3: CC, CE, TI o PA", example = "CC")
		@NotNull(message = "El tipo de documento es obligatorio")
		TipoDocumento tipoDocumento,

		@Schema(example = "1023456789")
		@NotBlank(message = "El número de documento es obligatorio")
		@Size(max = 20, message = "El número de documento no puede superar 20 caracteres")
		@Pattern(regexp = "^[A-Za-z0-9]+$", message = "El número de documento solo admite letras y dígitos")
		String numeroDocumento,

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
