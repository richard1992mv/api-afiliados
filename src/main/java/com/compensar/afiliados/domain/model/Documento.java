package com.compensar.afiliados.domain.model;

import com.compensar.afiliados.domain.exception.DatoInvalidoException;

import java.util.regex.Pattern;

/**
 * Value object: documento de identidad (tipo + número). Inmutable y siempre válido.
 */
public record Documento(TipoDocumento tipo, String numero) {

	private static final Pattern NUMERICO = Pattern.compile("^\\d{3,20}$");
	private static final Pattern ALFANUMERICO = Pattern.compile("^[A-Z0-9]{3,20}$");

	public Documento {
		if (tipo == null) {
			throw new DatoInvalidoException("El tipo de documento es obligatorio");
		}
		if (numero == null || numero.isBlank()) {
			throw new DatoInvalidoException("El número de documento es obligatorio");
		}
		numero = numero.trim().toUpperCase();
		Pattern formato = tipo.esSoloNumerico() ? NUMERICO : ALFANUMERICO;
		if (!formato.matcher(numero).matches()) {
			throw new DatoInvalidoException("El número de documento no tiene un formato válido para el tipo " + tipo);
		}
	}
}
