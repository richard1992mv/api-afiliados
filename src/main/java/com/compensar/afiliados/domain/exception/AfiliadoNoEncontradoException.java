package com.compensar.afiliados.domain.exception;

/**
 * No existe un afiliado con el identificador solicitado.
 */
public class AfiliadoNoEncontradoException extends AfiliadoException {

	private final Long id;

	public AfiliadoNoEncontradoException(Long id) {
		super("No existe un afiliado con id " + id);
		this.id = id;
	}

	public Long getId() {
		return id;
	}
}
