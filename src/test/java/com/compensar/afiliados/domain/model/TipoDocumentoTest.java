package com.compensar.afiliados.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TipoDocumentoTest {

	@Test
	void rn3SoloSePermitenCcCeTiYPa() {
		// Si alguien agrega o quita un tipo, este test falla y obliga a revisar la regla RN3.
		assertThat(TipoDocumento.values())
				.containsExactly(TipoDocumento.CC, TipoDocumento.CE, TipoDocumento.TI, TipoDocumento.PA);
	}

	@Test
	void rn3UnTipoDesconocidoNoExiste() {
		assertThatThrownBy(() -> TipoDocumento.valueOf("NIT")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void ccYTiSonSoloNumericosCeYPaNo() {
		assertThat(TipoDocumento.CC.esSoloNumerico()).isTrue();
		assertThat(TipoDocumento.TI.esSoloNumerico()).isTrue();
		assertThat(TipoDocumento.CE.esSoloNumerico()).isFalse();
		assertThat(TipoDocumento.PA.esSoloNumerico()).isFalse();
	}
}
