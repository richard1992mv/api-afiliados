package com.compensar.afiliados.domain.model;

import com.compensar.afiliados.domain.exception.AfiliadoInactivoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AfiliadoTest {

	private static final BigDecimal SMMLV = new BigDecimal("1423500");
	private static final Documento DOCUMENTO = new Documento(TipoDocumento.CC, "1023456789");
	private static final Email EMAIL = new Email("ana@correo.com");

	private static Afiliado afiliadoActivo() {
		return Afiliado.reconstituir(1L, DOCUMENTO, "Ana", "Pérez", EMAIL,
				new BigDecimal("2000000"), Categoria.A, EstadoAfiliado.ACTIVO);
	}

	private static Afiliado afiliadoInactivo() {
		return Afiliado.reconstituir(2L, DOCUMENTO, "Luis", "Gómez", EMAIL,
				new BigDecimal("2000000"), Categoria.A, EstadoAfiliado.INACTIVO);
	}

	@Test
	void registrarCreaUnAfiliadoActivoConCategoriaCalculada() {
		Afiliado afiliado = Afiliado.registrar(DOCUMENTO, "  Ana ", "Pérez", EMAIL, new BigDecimal("5694000"), SMMLV);

		assertThat(afiliado.getId()).isNull();
		assertThat(afiliado.getEstado()).isEqualTo(EstadoAfiliado.ACTIVO);
		assertThat(afiliado.estaActivo()).isTrue();
		assertThat(afiliado.getCategoria()).isEqualTo(Categoria.B);
		assertThat(afiliado.getNombres()).isEqualTo("Ana");
		assertThat(afiliado.getApellidos()).isEqualTo("Pérez");
		assertThat(afiliado.getDocumento()).isEqualTo(DOCUMENTO);
		assertThat(afiliado.getEmail()).isEqualTo(EMAIL);
		assertThat(afiliado.getSalario()).isEqualByComparingTo("5694000");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"   "})
	void registrarRechazaNombresVacios(String nombres) {
		assertThatThrownBy(() -> Afiliado.registrar(DOCUMENTO, nombres, "Pérez", EMAIL, new BigDecimal("1000000"), SMMLV))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("nombres");
	}

	@Test
	void registrarRechazaApellidosDemasiadoLargos() {
		String largo = "x".repeat(101);

		assertThatThrownBy(() -> Afiliado.registrar(DOCUMENTO, "Ana", largo, EMAIL, new BigDecimal("1000000"), SMMLV))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("apellidos");
	}

	@Test
	void registrarRechazaDocumentoYEmailNulos() {
		assertThatThrownBy(() -> Afiliado.registrar(null, "Ana", "Pérez", EMAIL, new BigDecimal("1000000"), SMMLV))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("documento");
		assertThatThrownBy(() -> Afiliado.registrar(DOCUMENTO, "Ana", "Pérez", null, new BigDecimal("1000000"), SMMLV))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("email");
	}

	@Test
	void reconstituirExigeId() {
		assertThatThrownBy(() -> Afiliado.reconstituir(null, DOCUMENTO, "Ana", "Pérez", EMAIL,
				new BigDecimal("1000000"), Categoria.A, EstadoAfiliado.ACTIVO))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("id");
	}

	@Test
	void actualizarCambiaLosDatosYRecalculaLaCategoria() {
		Afiliado afiliado = afiliadoActivo();
		Email nuevoEmail = new Email("ana.nueva@correo.com");

		afiliado.actualizar("Ana María", "Pérez Ruiz", nuevoEmail, new BigDecimal("5694000.01"), SMMLV);

		assertThat(afiliado.getNombres()).isEqualTo("Ana María");
		assertThat(afiliado.getApellidos()).isEqualTo("Pérez Ruiz");
		assertThat(afiliado.getEmail()).isEqualTo(nuevoEmail);
		assertThat(afiliado.getSalario()).isEqualByComparingTo("5694000.01");
		assertThat(afiliado.getCategoria()).isEqualTo(Categoria.C);
		assertThat(afiliado.getDocumento()).isEqualTo(DOCUMENTO);
	}

	@Test
	void actualizarConDatosInvalidosNoModificaNada() {
		Afiliado afiliado = afiliadoActivo();

		assertThatThrownBy(() -> afiliado.actualizar("Ana María", "Pérez", EMAIL, BigDecimal.ZERO, SMMLV))
				.isInstanceOf(IllegalArgumentException.class);

		assertThat(afiliado.getNombres()).isEqualTo("Ana");
		assertThat(afiliado.getSalario()).isEqualByComparingTo("2000000");
		assertThat(afiliado.getCategoria()).isEqualTo(Categoria.A);
	}

	@Test
	void rn4ActualizarUnAfiliadoInactivoLanzaExcepcion() {
		Afiliado afiliado = afiliadoInactivo();

		assertThatThrownBy(() -> afiliado.actualizar("Luis", "Gómez", EMAIL, new BigDecimal("3000000"), SMMLV))
				.isInstanceOf(AfiliadoInactivoException.class)
				.hasMessageContaining("2");
		assertThat(afiliado.getSalario()).isEqualByComparingTo("2000000");
	}

	@Test
	void inactivarCambiaElEstado() {
		Afiliado afiliado = afiliadoActivo();

		afiliado.inactivar();

		assertThat(afiliado.getEstado()).isEqualTo(EstadoAfiliado.INACTIVO);
		assertThat(afiliado.estaActivo()).isFalse();
	}

	@Test
	void rn4InactivarUnAfiliadoYaInactivoLanzaExcepcion() {
		Afiliado afiliado = afiliadoInactivo();

		assertThatThrownBy(afiliado::inactivar)
				.isInstanceOf(AfiliadoInactivoException.class)
				.satisfies(e -> assertThat(((AfiliadoInactivoException) e).getId()).isEqualTo(2L));
	}

	@Test
	void dosAfiliadosConElMismoIdSonIguales() {
		Afiliado uno = afiliadoActivo();
		Afiliado otro = Afiliado.reconstituir(1L, new Documento(TipoDocumento.PA, "XY99999"), "Otro", "Nombre",
				new Email("otro@correo.com"), new BigDecimal("9000000"), Categoria.C, EstadoAfiliado.INACTIVO);

		assertThat(uno).isEqualTo(otro).hasSameHashCodeAs(otro);
		assertThat(uno).isEqualTo(uno);
		assertThat(uno).isNotEqualTo(afiliadoInactivo());
		assertThat(uno).isNotEqualTo("no es un afiliado");
	}

	@Test
	void afiliadosNuevosSinIdSoloSonIgualesASiMismos() {
		Afiliado nuevo = Afiliado.registrar(DOCUMENTO, "Ana", "Pérez", EMAIL, new BigDecimal("1000000"), SMMLV);
		Afiliado otroNuevo = Afiliado.registrar(DOCUMENTO, "Ana", "Pérez", EMAIL, new BigDecimal("1000000"), SMMLV);

		assertThat(nuevo).isEqualTo(nuevo).isNotEqualTo(otroNuevo);
		assertThat(nuevo.hashCode()).isEqualTo(nuevo.hashCode());
	}
}
