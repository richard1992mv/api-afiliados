package com.compensar.afiliados.application.port.in;

import java.math.BigDecimal;

/**
 * Datos de entrada para actualizar un afiliado. El documento no se modifica: es su identidad.
 */
public record ActualizarAfiliadoComando(
		String nombres,
		String apellidos,
		String email,
		BigDecimal salario) {
}
