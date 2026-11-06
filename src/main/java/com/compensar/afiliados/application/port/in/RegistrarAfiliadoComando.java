package com.compensar.afiliados.application.port.in;

import com.compensar.afiliados.domain.model.TipoDocumento;

import java.math.BigDecimal;

/**
 * Datos de entrada para registrar un afiliado. Se validan al convertirse en objetos de dominio.
 */
public record RegistrarAfiliadoComando(
		TipoDocumento tipoDocumento,
		String numeroDocumento,
		String nombres,
		String apellidos,
		String email,
		BigDecimal salario) {
}
