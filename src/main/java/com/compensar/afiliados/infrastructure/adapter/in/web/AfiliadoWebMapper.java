package com.compensar.afiliados.infrastructure.adapter.in.web;

import com.compensar.afiliados.application.port.in.ActualizarAfiliadoComando;
import com.compensar.afiliados.application.port.in.RegistrarAfiliadoComando;
import com.compensar.afiliados.domain.model.Afiliado;

/**
 * Traduce entre los DTO HTTP y los comandos/modelo del núcleo.
 */
final class AfiliadoWebMapper {

	private AfiliadoWebMapper() {
	}

	static RegistrarAfiliadoComando aComando(RegistrarAfiliadoRequest request) {
		return new RegistrarAfiliadoComando(request.tipoDocumento(), request.numeroDocumento(), request.nombres(),
				request.apellidos(), request.email(), request.salario());
	}

	static ActualizarAfiliadoComando aComando(ActualizarAfiliadoRequest request) {
		return new ActualizarAfiliadoComando(request.nombres(), request.apellidos(), request.email(),
				request.salario());
	}

	static AfiliadoResponse aResponse(Afiliado afiliado) {
		return new AfiliadoResponse(
				afiliado.getId(),
				afiliado.getDocumento().tipo(),
				afiliado.getDocumento().numero(),
				afiliado.getNombres(),
				afiliado.getApellidos(),
				afiliado.getEmail().valor(),
				afiliado.getSalario(),
				afiliado.getCategoria(),
				afiliado.getEstado());
	}
}
