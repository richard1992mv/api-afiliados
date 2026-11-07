package com.compensar.afiliados.application.service;

import com.compensar.afiliados.application.port.out.AfiliadoRepositoryPort;
import com.compensar.afiliados.domain.exception.AfiliadoNoEncontradoException;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarAfiliadoServiceTest {

	@Mock
	private AfiliadoRepositoryPort repositorio;

	@InjectMocks
	private ConsultarAfiliadoService servicio;

	@Test
	void devuelveElAfiliadoCuandoExiste() {
		Afiliado afiliado = Afiliado.reconstituir(1L, new Documento(TipoDocumento.CC, "123456"), "Ana", "Pérez",
				new Email("ana@correo.com"), new BigDecimal("2000000"), Categoria.A, EstadoAfiliado.ACTIVO);
		when(repositorio.buscarPorId(1L)).thenReturn(Optional.of(afiliado));

		assertThat(servicio.consultar(1L)).isSameAs(afiliado);
	}

	@Test
	void lanzaExcepcionCuandoNoExiste() {
		when(repositorio.buscarPorId(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servicio.consultar(99L))
				.isInstanceOf(AfiliadoNoEncontradoException.class)
				.hasMessageContaining("99")
				.satisfies(e -> assertThat(((AfiliadoNoEncontradoException) e).getId()).isEqualTo(99L));
	}

	@Test
	void rechazaIdNulo() {
		assertThatThrownBy(() -> servicio.consultar(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void exigeRepositorio() {
		assertThatThrownBy(() -> new ConsultarAfiliadoService(null)).isInstanceOf(NullPointerException.class);
	}
}
