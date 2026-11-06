package com.compensar.afiliados.infrastructure.adapter.out.persistence;

import com.compensar.afiliados.MySqlTestcontainersConfig;
import com.compensar.afiliados.domain.exception.DocumentoDuplicadoException;
import com.compensar.afiliados.domain.model.Afiliado;
import com.compensar.afiliados.domain.model.Categoria;
import com.compensar.afiliados.domain.model.Documento;
import com.compensar.afiliados.domain.model.Email;
import com.compensar.afiliados.domain.model.EstadoAfiliado;
import com.compensar.afiliados.domain.model.TipoDocumento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integración real con MySQL 8 (Testcontainers): migración Flyway, mapeo JPA y restricciones.
 * Cada test corre en una transacción que se revierte al terminar.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({MySqlTestcontainersConfig.class, AfiliadoPersistenceAdapter.class})
class AfiliadoPersistenceAdapterIntegrationTest {

	private static final BigDecimal SMMLV = new BigDecimal("1750905");

	@Autowired
	private AfiliadoPersistenceAdapter adaptador;

	@Autowired
	private JdbcTemplate jdbc;

	private static Afiliado nuevo(TipoDocumento tipo, String numero) {
		return Afiliado.registrar(new Documento(tipo, numero), "José", "Muñoz Peña",
				new Email("jose.munoz@correo.com"), new BigDecimal("4000000.50"), SMMLV);
	}

	@Test
	void flywayAplicoLaMigracionV1() {
		Integer version = jdbc.queryForObject(
				"SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND success = 1", Integer.class);

		assertThat(version).isEqualTo(1);
	}

	@Test
	void guardaYLeeUnAfiliadoSinPerderDatos() {
		Afiliado guardado = adaptador.guardar(nuevo(TipoDocumento.CC, "1023456789"));

		Afiliado leido = adaptador.buscarPorId(guardado.getId()).orElseThrow();

		assertThat(guardado.getId()).isNotNull();
		// utf8mb4 conserva tildes y eñes; DECIMAL(15,2) conserva los centavos.
		// 4.000.000,50 está entre 2 y 4 SMMLV (3.501.810 y 7.003.620): categoría B.
		assertThat(leido.getNombres()).isEqualTo("José");
		assertThat(leido.getApellidos()).isEqualTo("Muñoz Peña");
		assertThat(leido.getSalario()).isEqualByComparingTo("4000000.50");
		assertThat(leido.getCategoria()).isEqualTo(Categoria.B);
		assertThat(leido.getEstado()).isEqualTo(EstadoAfiliado.ACTIVO);
		assertThat(leido.getDocumento()).isEqualTo(new Documento(TipoDocumento.CC, "1023456789"));
	}

	@Test
	void guardarUnAfiliadoExistentePersisteLosCambios() {
		Afiliado guardado = adaptador.guardar(nuevo(TipoDocumento.CC, "1023456789"));

		guardado.inactivar();
		adaptador.guardar(guardado);

		assertThat(adaptador.buscarPorId(guardado.getId())).get()
				.extracting(Afiliado::getEstado).isEqualTo(EstadoAfiliado.INACTIVO);
	}

	@Test
	void rn1ExistePorDocumentoDistingueTipoYNumero() {
		adaptador.guardar(nuevo(TipoDocumento.CC, "1023456789"));

		assertThat(adaptador.existePorDocumento(new Documento(TipoDocumento.CC, "1023456789"))).isTrue();
		assertThat(adaptador.existePorDocumento(new Documento(TipoDocumento.TI, "1023456789"))).isFalse();
		assertThat(adaptador.existePorDocumento(new Documento(TipoDocumento.CC, "999999"))).isFalse();
	}

	@Test
	void rn1ElIndiceUnicoDeMySqlSeTraduceADocumentoDuplicado() {
		// Simula dos registros concurrentes que ya pasaron la validación del servicio.
		adaptador.guardar(nuevo(TipoDocumento.CC, "1023456789"));

		assertThatThrownBy(() -> adaptador.guardar(nuevo(TipoDocumento.CC, "1023456789")))
				.isInstanceOf(DocumentoDuplicadoException.class)
				.hasMessageNotContaining("1023456789");
	}

	@Test
	void rn1ElMismoNumeroConOtroTipoSeGuarda() {
		adaptador.guardar(nuevo(TipoDocumento.CC, "1023456789"));

		Afiliado otro = adaptador.guardar(nuevo(TipoDocumento.TI, "1023456789"));

		assertThat(otro.getId()).isNotNull();
	}

	@Test
	void listarDevuelveLosAfiliadosOrdenadosPorId() {
		Afiliado primero = adaptador.guardar(nuevo(TipoDocumento.CC, "111111"));
		Afiliado segundo = adaptador.guardar(nuevo(TipoDocumento.PA, "AB1234"));

		assertThat(adaptador.listar()).extracting(Afiliado::getId)
				.containsSubsequence(primero.getId(), segundo.getId());
	}

	@Test
	void rn3LaBaseRechazaUnTipoDeDocumentoNoPermitidoAunqueSeSalteLaApi() {
		// Defensa en profundidad: el CHECK protege la tabla incluso frente a un INSERT directo.
		// MySQL reporta el CHECK con el error 3819, que Spring expone como DataAccessException genérica.
		assertThatThrownBy(() -> jdbc.update("""
				INSERT INTO afiliados (tipo_documento, numero_documento, nombres, apellidos, email, salario, categoria, estado)
				VALUES ('NI', '900123', 'X', 'Y', 'x@correo.com', 1000000, 'A', 'ACTIVO')"""))
				.isInstanceOf(DataAccessException.class)
				.hasMessageContaining("ck_afiliados_tipo_documento");
	}

	@Test
	void laBaseRechazaUnSalarioNoPositivoAunqueSeSalteLaApi() {
		assertThatThrownBy(() -> jdbc.update("""
				INSERT INTO afiliados (tipo_documento, numero_documento, nombres, apellidos, email, salario, categoria, estado)
				VALUES ('CC', '900124', 'X', 'Y', 'x@correo.com', 0, 'A', 'ACTIVO')"""))
				.isInstanceOf(DataAccessException.class)
				.hasMessageContaining("ck_afiliados_salario");
	}
}
