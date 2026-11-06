package com.compensar.afiliados.domain.model;

/**
 * Tipos de documento de identidad admitidos.
 */
public enum TipoDocumento {

	/** Cédula de ciudadanía: solo dígitos. */
	CC(true),
	/** Cédula de extranjería. */
	CE(false),
	/** Tarjeta de identidad: solo dígitos. */
	TI(true),
	/** Pasaporte: alfanumérico. */
	PA(false);

	private final boolean soloNumerico;

	TipoDocumento(boolean soloNumerico) {
		this.soloNumerico = soloNumerico;
	}

	public boolean esSoloNumerico() {
		return soloNumerico;
	}
}
