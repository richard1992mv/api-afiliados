package com.compensar.afiliados.application.service;

import com.compensar.afiliados.application.port.in.ConsultarAfiliadoUseCase;
import com.compensar.afiliados.application.port.out.AfiliadoRepositoryPort;
import com.compensar.afiliados.domain.exception.AfiliadoNoEncontradoException;
import com.compensar.afiliados.domain.model.Afiliado;

import java.util.Objects;

public class ConsultarAfiliadoService implements ConsultarAfiliadoUseCase {

	private final AfiliadoRepositoryPort repositorio;

	public ConsultarAfiliadoService(AfiliadoRepositoryPort repositorio) {
		this.repositorio = Objects.requireNonNull(repositorio, "El repositorio es obligatorio");
	}

	@Override
	public Afiliado consultar(Long id) {
		Objects.requireNonNull(id, "El id es obligatorio");
		return repositorio.buscarPorId(id)
				.orElseThrow(() -> new AfiliadoNoEncontradoException(id));
	}
}
