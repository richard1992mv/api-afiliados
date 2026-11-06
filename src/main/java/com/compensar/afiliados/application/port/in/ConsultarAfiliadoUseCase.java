package com.compensar.afiliados.application.port.in;

import com.compensar.afiliados.domain.model.Afiliado;

/**
 * Caso de uso: consultar un afiliado por su identificador.
 */
public interface ConsultarAfiliadoUseCase {

	Afiliado consultar(Long id);
}
