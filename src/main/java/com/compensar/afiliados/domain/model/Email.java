package com.compensar.afiliados.domain.model;

import com.compensar.afiliados.domain.exception.DatoInvalidoException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Value object: correo electrónico normalizado en minúsculas. Inmutable y siempre válido.
 */
public record Email(String valor) {

	private static final int LONGITUD_MAXIMA = 254;
	private static final Pattern FORMATO =
			Pattern.compile("^[a-z0-9._%+-]+@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.[a-z]{2,}$");

	public Email {
		if (valor == null || valor.isBlank()) {
			throw new DatoInvalidoException("El email es obligatorio");
		}
		valor = valor.trim().toLowerCase(Locale.ROOT);
		// La longitud se valida antes de la expresión regular para acotar su costo.
		if (valor.length() > LONGITUD_MAXIMA || !FORMATO.matcher(valor).matches()) {
			throw new DatoInvalidoException("El email no tiene un formato válido");
		}
	}
}
