package com.compensar.afiliados.application.service;

import com.compensar.afiliados.application.port.in.ListarAfiliadosUseCase;
import com.compensar.afiliados.application.port.out.AfiliadoRepositoryPort;
import com.compensar.afiliados.domain.model.Afiliado;

import java.util.List;
import java.util.Objects;

public class ListarAfiliadosService implements ListarAfiliadosUseCase {

	private final AfiliadoRepositoryPort repositorio;

	public ListarAfiliadosService(AfiliadoRepositoryPort repositorio) {
		this.repositorio = Objects.requireNonNull(repositorio, "El repositorio es obligatorio");
	}

	@Override
	public List<Afiliado> listar() {
		return List.copyOf(repositorio.listar());
	}
}
