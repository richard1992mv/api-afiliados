package com.compensar.afiliados.application.port.in;

import com.compensar.afiliados.domain.model.Afiliado;

/**
 * Caso de uso: actualizar los datos de un afiliado activo (RN2 recálculo, RN4).
 */
public interface ActualizarAfiliadoUseCase {

	Afiliado actualizar(Long id, ActualizarAfiliadoComando comando);
}
