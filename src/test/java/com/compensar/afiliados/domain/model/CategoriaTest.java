package com.compensar.afiliados.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoriaTest {

	private static final BigDecimal SMMLV = new BigDecimal("1423500");

	@ParameterizedTest(name = "salario {0} -> categoría {1}")
	@CsvSource({
			"0.01,        A",
			"1423500,     A",
			"2847000,     A", // exactamente 2 SMMLV: límite superior de A
			"2847000.01,  B", // un centavo por encima de 2 SMMLV
			"4000000,     B",
			"5694000,     B", // exactamente 4 SMMLV: límite superior de B
			"5694000.01,  C", // un centavo por encima de 4 SMMLV
			"20000000,    C"
	})
	void calculaLaCategoriaSegunElSalario(BigDecimal salario, Categoria esperada) {
		assertThat(Categoria.calcular(salario, SMMLV)).isEqualTo(esperada);
	}

	@Test
	void tratanIgualLosLimitesConDistintaEscala() {
		// 2847000.00 y 2847000 son iguales con compareTo, aunque equals diga lo contrario.
		assertThat(Categoria.calcular(new BigDecimal("2847000.00"), SMMLV)).isEqualTo(Categoria.A);
		assertThat(Categoria.calcular(new BigDecimal("5694000.000"), SMMLV)).isEqualTo(Categoria.B);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"0", "-1", "-0.01"})
	void rechazaSalarioNuloOMenorOIgualACero(BigDecimal salario) {
		assertThatThrownBy(() -> Categoria.calcular(salario, SMMLV))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("salario");
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"0", "-1423500"})
	void rechazaSmmlvNuloOMenorOIgualACero(BigDecimal smmlv) {
		assertThatThrownBy(() -> Categoria.calcular(new BigDecimal("1000000"), smmlv))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("SMMLV");
	}
}
