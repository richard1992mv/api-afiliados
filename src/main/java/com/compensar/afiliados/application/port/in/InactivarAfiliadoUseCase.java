package com.compensar.afiliados.application.port.in;

import com.compensar.afiliados.domain.model.Afiliado;

/**
 * Caso de uso: inactivar un afiliado activo (RN4).
 */
public interface InactivarAfiliadoUseCase {

	Afiliado inactivar(Long id);
}
