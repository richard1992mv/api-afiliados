package com.compensar.afiliados.domain.model;

import java.math.BigDecimal;

/**
 * Categoría del afiliado según su salario expresado en SMMLV.
 */
public enum Categoria {

	/** Salario hasta 2 SMMLV (inclusive). */
	A,
	/** Salario mayor a 2 y hasta 4 SMMLV (inclusive). */
	B,
	/** Salario mayor a 4 SMMLV. */
	C;

	private static final BigDecimal LIMITE_A = BigDecimal.valueOf(2);
	private static final BigDecimal LIMITE_B = BigDecimal.valueOf(4);

	/**
	 * RN2: calcula la categoría a partir del salario y el SMMLV vigente.
	 * Se usa {@code compareTo} y no {@code equals} para que 2847000 y 2847000.00 se traten igual.
	 */
	public static Categoria calcular(BigDecimal salario, BigDecimal smmlv) {
		validarSmmlv(smmlv);
		if (salario == null || salario.signum() <= 0) {
			throw new IllegalArgumentException("El salario debe ser mayor que cero");
		}
		if (salario.compareTo(smmlv.multiply(LIMITE_A)) <= 0) {
			return A;
		}
		if (salario.compareTo(smmlv.multiply(LIMITE_B)) <= 0) {
			return B;
		}
		return C;
	}

	/**
	 * Valida que el SMMLV sea un valor positivo y lo devuelve.
	 */
	public static BigDecimal validarSmmlv(BigDecimal smmlv) {
		if (smmlv == null || smmlv.signum() <= 0) {
			throw new IllegalArgumentException("El SMMLV debe ser mayor que cero");
		}
		return smmlv;
	}
}
