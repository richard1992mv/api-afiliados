package com.compensar.afiliados.application.service;

import com.compensar.afiliados.application.port.in.InactivarAfiliadoUseCase;
import com.compensar.afiliados.application.port.out.AfiliadoRepositoryPort;
import com.compensar.afiliados.domain.exception.AfiliadoNoEncontradoException;
import com.compensar.afiliados.domain.model.Afiliado;

import java.util.Objects;

public class InactivarAfiliadoService implements InactivarAfiliadoUseCase {

	private final AfiliadoRepositoryPort repositorio;

	public InactivarAfiliadoService(AfiliadoRepositoryPort repositorio) {
		this.repositorio = Objects.requireNonNull(repositorio, "El repositorio es obligatorio");
	}

	@Override
	public Afiliado inactivar(Long id) {
		Objects.requireNonNull(id, "El id es obligatorio");
		Afiliado afiliado = repositorio.buscarPorId(id)
				.orElseThrow(() -> new AfiliadoNoEncontradoException(id));

		// RN4 se aplica dentro de la entidad.
		afiliado.inactivar();
		return repositorio.guardar(afiliado);
	}
}
