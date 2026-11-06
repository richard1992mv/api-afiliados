package com.compensar.afiliados.domain.exception;

/**
 * RN4: un afiliado inactivo no se puede modificar.
 */
public class AfiliadoInactivoException extends AfiliadoException {

	private final Long id;

	public AfiliadoInactivoException(Long id) {
		super("El afiliado con id " + id + " está inactivo y no se puede modificar");
		this.id = id;
	}

	public Long getId() {
		return id;
	}
}
