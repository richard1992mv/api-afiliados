package com.compensar.afiliados.infrastructure.adapter.in.web;

import com.compensar.afiliados.domain.model.Categoria;
import com.compensar.afiliados.domain.model.EstadoAfiliado;
import com.compensar.afiliados.domain.model.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Representación pública de un afiliado: un contrato explícito, independiente de la tabla.
 */
@Schema(description = "Afiliado")
public record AfiliadoResponse(
		Long id,
		TipoDocumento tipoDocumento,
		String numeroDocumento,
		String nombres,
		String apellidos,
		String email,
		BigDecimal salario,
		@Schema(description = "RN2: A hasta 2 SMMLV, B hasta 4, C más de 4")
		Categoria categoria,
		EstadoAfiliado estado) {
}
