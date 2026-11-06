package com.compensar.afiliados.application.port.in;

import com.compensar.afiliados.domain.model.Afiliado;

import java.util.List;

/**
 * Caso de uso: listar los afiliados.
 */
public interface ListarAfiliadosUseCase {

	List<Afiliado> listar();
}
