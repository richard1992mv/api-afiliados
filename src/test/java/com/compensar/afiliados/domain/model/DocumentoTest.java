package com.compensar.afiliados.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentoTest {

	@ParameterizedTest(name = "{0} {1} es válido")
	@CsvSource({
			"CC, 1023456789",
			"TI, 1001234567",
			"CE, 123456",
			"PA, AB123456"
	})
	void aceptaDocumentosValidos(TipoDocumento tipo, String numero) {
		Documento documento = new Documento(tipo, numero);

		assertThat(documento.tipo()).isEqualTo(tipo);
		assertThat(documento.numero()).isEqualTo(numero);
	}

	@Test
	void normalizaEspaciosYMayusculas() {
		assertThat(new Documento(TipoDocumento.PA, "  ab123456 ").numero()).isEqualTo("AB123456");
	}

	@Test
	void dosDocumentosConMismoTipoYNumeroSonIguales() {
		assertThat(new Documento(TipoDocumento.CC, "123456"))
				.isEqualTo(new Documento(TipoDocumento.CC, " 123456 "));
	}

	@Test
	void rechazaTipoNulo() {
		assertThatThrownBy(() -> new Documento(null, "123456"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("tipo");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"   "})
	void rechazaNumeroVacio(String numero) {
		assertThatThrownBy(() -> new Documento(TipoDocumento.CC, numero))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("obligatorio");
	}

	@ParameterizedTest(name = "{0} {1} es inválido")
	@CsvSource({
			"CC, ABC123",                 // la cédula solo admite dígitos
			"TI, 12A456",                 // la tarjeta de identidad solo admite dígitos
			"CC, 12",                     // muy corto
			"PA, AB-123",                 // caracteres especiales
			"CE, 123456789012345678901"   // más de 20 caracteres
	})
	void rechazaFormatosInvalidos(TipoDocumento tipo, String numero) {
		assertThatThrownBy(() -> new Documento(tipo, numero))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("formato");
	}
}
