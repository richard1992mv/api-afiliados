package com.compensar.afiliados.application.port.in;

import com.compensar.afiliados.domain.model.Afiliado;

/**
 * Caso de uso: registrar un nuevo afiliado (RN1 documento único, RN2 categoría).
 */
public interface RegistrarAfiliadoUseCase {

	Afiliado registrar(RegistrarAfiliadoComando comando);
}
