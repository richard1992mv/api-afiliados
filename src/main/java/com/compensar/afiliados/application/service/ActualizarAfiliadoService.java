package com.compensar.afiliados.application.service;

import com.compensar.afiliados.application.port.in.ActualizarAfiliadoComando;
import com.compensar.afiliados.application.port.in.ActualizarAfiliadoUseCase;
import com.compensar.afiliados.application.port.out.AfiliadoRepositoryPort;
import com.compensar.afiliados.domain.exception.AfiliadoNoEncontradoException;
import com.compensar.afiliados.domain.model.Afiliado;
import com.compensar.afiliados.domain.model.Categoria;
import com.compensar.afiliados.domain.model.Email;

import java.math.BigDecimal;
import java.util.Objects;

public class ActualizarAfiliadoService implements ActualizarAfiliadoUseCase {

	private final AfiliadoRepositoryPort repositorio;
	private final BigDecimal smmlv;

	public ActualizarAfiliadoService(AfiliadoRepositoryPort repositorio, BigDecimal smmlv) {
		this.repositorio = Objects.requireNonNull(repositorio, "El repositorio es obligatorio");
		this.smmlv = Categoria.validarSmmlv(smmlv);
	}

	@Override
	public Afiliado actualizar(Long id, ActualizarAfiliadoComando comando) {
		Objects.requireNonNull(id, "El id es obligatorio");
		Objects.requireNonNull(comando, "El comando es obligatorio");
		Afiliado afiliado = repositorio.buscarPorId(id)
				.orElseThrow(() -> new AfiliadoNoEncontradoException(id));

		// RN4 y RN2 se aplican dentro de la entidad.
		afiliado.actualizar(comando.nombres(), comando.apellidos(), new Email(comando.email()),
				comando.salario(), smmlv);
		return repositorio.guardar(afiliado);
	}
}
