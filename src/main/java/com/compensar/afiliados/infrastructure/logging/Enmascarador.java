package com.compensar.afiliados.infrastructure.logging;

/**
 * Enmascara datos personales antes de escribirlos en logs (Ley 1581 de 2012).
 * Los logs se copian, se centralizan y los lee más gente que la base de datos:
 * nunca deben contener el documento ni el email completos.
 */
public final class Enmascarador {

	private static final int DIGITOS_VISIBLES = 4;
	private static final String MASCARA = "****";

	private Enmascarador() {
	}

	/** {@code 1023456789 -> ******6789}; valores cortos se ocultan por completo. */
	public static String documento(String numero) {
		if (numero == null || numero.length() <= DIGITOS_VISIBLES) {
			return MASCARA;
		}
		int ocultos = numero.length() - DIGITOS_VISIBLES;
		return "*".repeat(ocultos) + numero.substring(ocultos);
	}

	/** {@code ana.perez@correo.com -> a***@correo.com}. */
	public static String email(String email) {
		if (email == null) {
			return MASCARA;
		}
		int arroba = email.indexOf('@');
		if (arroba <= 0) {
			return MASCARA;
		}
		return email.charAt(0) + "***" + email.substring(arroba);
	}
}
