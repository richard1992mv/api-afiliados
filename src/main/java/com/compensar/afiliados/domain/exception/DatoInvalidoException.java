package com.compensar.afiliados.domain.exception;

/**
 * Un dato no cumple las reglas del dominio (formato, obligatoriedad, rango).
 * Extiende IllegalArgumentException por semántica, pero tiene un tipo propio para que la capa
 * web solo exponga al cliente mensajes del dominio y no los de cualquier librería.
 */
public class DatoInvalidoException extends IllegalArgumentException {

	public DatoInvalidoException(String mensaje) {
		super(mensaje);
	}
}
