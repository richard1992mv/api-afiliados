package com.compensar.afiliados.infrastructure.adapter.out.persistence;

import com.compensar.afiliados.domain.exception.DocumentoDuplicadoException;
import com.compensar.afiliados.domain.model.Afiliado;
import com.compensar.afiliados.domain.model.Categoria;
import com.compensar.afiliados.domain.model.Documento;
import com.compensar.afiliados.domain.model.Email;
import com.compensar.afiliados.domain.model.EstadoAfiliado;
import com.compensar.afiliados.domain.model.TipoDocumento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AfiliadoPersistenceAdapterTest {

	private static final Documento DOCUMENTO = new Documento(TipoDocumento.CC, "1023456789");

	@Mock
	private AfiliadoJpaRepository repositorio;

	@InjectMocks
	private AfiliadoPersistenceAdapter adaptador;

	private static Afiliado nuevo() {
		return Afiliado.registrar(DOCUMENTO, "Ana", "Pérez", new Email("ana@correo.com"),
				new BigDecimal("2000000"), new BigDecimal("1423500"));
	}

	private static AfiliadoEntity entidad(Long id) {
		return new AfiliadoEntity(id, TipoDocumento.CC, "1023456789", "Ana", "Pérez", "ana@correo.com",
				new BigDecimal("2000000"), Categoria.A, EstadoAfiliado.ACTIVO);
	}

	@Test
	void guardarDevuelveElAfiliadoConId() {
		when(repositorio.saveAndFlush(any())).thenReturn(entidad(5L));

		Afiliado guardado = adaptador.guardar(nuevo());

		assertThat(guardado.getId()).isEqualTo(5L);
		assertThat(guardado.getDocumento()).isEqualTo(DOCUMENTO);
	}

	@Test
	void rn1LaViolacionDelIndiceUnicoSeTraduceADocumentoDuplicado() {
		// Simula la carrera: el servicio validó, pero otra petición insertó el mismo documento antes.
		SQLIntegrityConstraintViolationException causa = new SQLIntegrityConstraintViolationException(
				"Duplicate entry 'CC-1023456789' for key 'afiliados.uk_afiliados_documento'");
		when(repositorio.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("error", causa));

		assertThatThrownBy(() -> adaptador.guardar(nuevo()))
				.isInstanceOf(DocumentoDuplicadoException.class)
				// El mensaje de MySQL, que trae el número de documento, no se propaga.
				.hasMessageNotContaining("1023456789");
	}

	@Test
	void otraViolacionDeIntegridadSePropagaSinTraducir() {
		DataIntegrityViolationException otra = new DataIntegrityViolationException("error",
				new SQLIntegrityConstraintViolationException("Check constraint 'ck_afiliados_salario' is violated"));
		when(repositorio.saveAndFlush(any())).thenThrow(otra);

		assertThatThrownBy(() -> adaptador.guardar(nuevo())).isSameAs(otra);
	}

	@Test
	void buscarPorIdMapeaLaEntidadOVacio() {
		when(repositorio.findById(5L)).thenReturn(Optional.of(entidad(5L)));
		when(repositorio.findById(6L)).thenReturn(Optional.empty());

		assertThat(adaptador.buscarPorId(5L)).get().extracting(Afiliado::getId).isEqualTo(5L);
		assertThat(adaptador.buscarPorId(6L)).isEmpty();
	}

	@Test
	void listarDevuelveLosAfiliadosOrdenadosPorId() {
		when(repositorio.findAll(Sort.by("id"))).thenReturn(List.of(entidad(1L), entidad(2L)));

		assertThat(adaptador.listar()).extracting(Afiliado::getId).containsExactly(1L, 2L);
	}

	@Test
	void existePorDocumentoConsultaPorTipoYNumero() {
		when(repositorio.existsByTipoDocumentoAndNumeroDocumento(TipoDocumento.CC, "1023456789")).thenReturn(true);
		when(repositorio.existsByTipoDocumentoAndNumeroDocumento(TipoDocumento.TI, "1023456789")).thenReturn(false);

		assertThat(adaptador.existePorDocumento(DOCUMENTO)).isTrue();
		assertThat(adaptador.existePorDocumento(new Documento(TipoDocumento.TI, "1023456789"))).isFalse();
	}
}
