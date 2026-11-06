package com.compensar.afiliados.infrastructure.logging;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class EnmascaradorTest {

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
			"1023456789, ******6789",
			"AB123456,   ****3456",
			"12345,      *2345"
	})
	void documentoMuestraSoloLosUltimosCuatroCaracteres(String numero, String esperado) {
		assertThat(Enmascarador.documento(numero)).isEqualTo(esperado);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"", "123", "1234"})
	void documentoCortoONuloSeOcultaPorCompleto(String numero) {
		assertThat(Enmascarador.documento(numero)).isEqualTo("****");
	}

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
			"ana.perez@correo.com, a***@correo.com",
			"b@empresa.co,         b***@empresa.co"
	})
	void emailMuestraLaPrimeraLetraYElDominio(String email, String esperado) {
		assertThat(Enmascarador.email(email)).isEqualTo(esperado);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"sin-arroba", "@correo.com"})
	void emailInvalidoONuloSeOcultaPorCompleto(String email) {
		assertThat(Enmascarador.email(email)).isEqualTo("****");
	}
}
