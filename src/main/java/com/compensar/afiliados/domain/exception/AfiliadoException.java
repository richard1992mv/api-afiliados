package com.compensar.afiliados.domain.exception;

/**
 * Base de las excepciones de negocio del dominio de afiliados.
 */
public abstract class AfiliadoException extends RuntimeException {

	protected AfiliadoException(String mensaje) {
		super(mensaje);
	}
}
