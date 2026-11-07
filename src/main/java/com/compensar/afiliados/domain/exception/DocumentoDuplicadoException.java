package com.compensar.afiliados.domain.exception;

import com.compensar.afiliados.domain.model.Documento;

/**
 * RN1: ya existe un afiliado con el mismo tipo y número de documento.
 * El mensaje no incluye el número para no filtrar datos personales en logs o respuestas.
 */
public class DocumentoDuplicadoException extends AfiliadoException {

	private final transient Documento documento;

	public DocumentoDuplicadoException(Documento documento) {
		super("Ya existe un afiliado con el tipo y número de documento indicados");
		this.documento = documento;
	}

	public Documento getDocumento() {
		return documento;
	}
}
