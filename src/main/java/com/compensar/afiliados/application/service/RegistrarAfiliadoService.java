package com.compensar.afiliados.application.service;

import com.compensar.afiliados.application.port.in.RegistrarAfiliadoComando;
import com.compensar.afiliados.application.port.in.RegistrarAfiliadoUseCase;
import com.compensar.afiliados.application.port.out.AfiliadoRepositoryPort;
import com.compensar.afiliados.domain.exception.DocumentoDuplicadoException;
import com.compensar.afiliados.domain.model.Afiliado;
import com.compensar.afiliados.domain.model.Categoria;
import com.compensar.afiliados.domain.model.Documento;
import com.compensar.afiliados.domain.model.Email;

import java.math.BigDecimal;
import java.util.Objects;

public class RegistrarAfiliadoService implements RegistrarAfiliadoUseCase {

	private final AfiliadoRepositoryPort repositorio;
	private final BigDecimal smmlv;

	public RegistrarAfiliadoService(AfiliadoRepositoryPort repositorio, BigDecimal smmlv) {
		this.repositorio = Objects.requireNonNull(repositorio, "El repositorio es obligatorio");
		this.smmlv = Categoria.validarSmmlv(smmlv);
	}

	@Override
	public Afiliado registrar(RegistrarAfiliadoComando comando) {
		Objects.requireNonNull(comando, "El comando es obligatorio");
		// 1. Validar la entrada construyendo los value objects, antes de tocar el repositorio.
		Documento documento = new Documento(comando.tipoDocumento(), comando.numeroDocumento());
		Email email = new Email(comando.email());
		Afiliado afiliado = Afiliado.registrar(documento, comando.nombres(), comando.apellidos(), email,
				comando.salario(), smmlv);

		// 2. RN1: el documento debe ser único.
		if (repositorio.existePorDocumento(documento)) {
			throw new DocumentoDuplicadoException(documento);
		}
		return repositorio.guardar(afiliado);
	}
}
