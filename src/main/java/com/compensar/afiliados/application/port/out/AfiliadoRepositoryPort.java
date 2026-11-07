package com.compensar.afiliados.application.port.out;

import com.compensar.afiliados.domain.model.Afiliado;
import com.compensar.afiliados.domain.model.Documento;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: lo que el núcleo necesita para persistir afiliados.
 * Lo implementa un adaptador de infraestructura (JPA) en la Fase 2.
 */
public interface AfiliadoRepositoryPort {

	/** Guarda un afiliado nuevo o existente y devuelve la versión persistida (con id). */
	Afiliado guardar(Afiliado afiliado);

	Optional<Afiliado> buscarPorId(Long id);

	List<Afiliado> listar();

	boolean existePorDocumento(Documento documento);
}
