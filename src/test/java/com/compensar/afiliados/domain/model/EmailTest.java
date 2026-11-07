package com.compensar.afiliados.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

	@ParameterizedTest
	@ValueSource(strings = {"ana@correo.com", "ana.perez+caja@empresa.com.co", "a_b-c@sub.dominio.org", "a@b.co",
			"ana@mi-empresa.gov.co"})
	void aceptaEmailsValidos(String valor) {
		assertThat(new Email(valor).valor()).isEqualTo(valor);
	}

	@Test
	void normalizaAMinusculasYQuitaEspacios() {
		assertThat(new Email("  Ana.Perez@Correo.COM ").valor()).isEqualTo("ana.perez@correo.com");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"  "})
	void rechazaEmailVacio(String valor) {
		assertThatThrownBy(() -> new Email(valor))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("obligatorio");
	}

	@ParameterizedTest
	@ValueSource(strings = {"sin-arroba.com", "ana@", "@correo.com", "ana@correo", "ana@correo.c", "ana perez@correo.com",
			"ana@correo..com", "ana@.correo.com", "ana@correo.com.", "ana@correo.c0m", "ana@@correo.com"})
	void rechazaFormatosInvalidos(String valor) {
		assertThatThrownBy(() -> new Email(valor))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("formato");
	}

	@Test
	void rechazaEmailDemasiadoLargo() {
		String largo = "a".repeat(250) + "@correo.com";

		assertThatThrownBy(() -> new Email(largo))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
