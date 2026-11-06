package com.compensar.afiliados.application.service;

import com.compensar.afiliados.application.port.out.AfiliadoRepositoryPort;
import com.compensar.afiliados.domain.exception.AfiliadoInactivoException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InactivarAfiliadoServiceTest {

	@Mock
	private AfiliadoRepositoryPort repositorio;

	@InjectMocks
	private InactivarAfiliadoService servicio;

	private static Afiliado afiliado(EstadoAfiliado estado) {
		return Afiliado.reconstituir(1L, new Documento(TipoDocumento.CC, "123456"), "Ana", "Pérez",
				new Email("ana@correo.com"), new BigDecimal("2000000"), Categoria.A, estado);
	}

	@Test
	void inactivaUnAfiliadoActivo() {
		Afiliado existente = afiliado(EstadoAfiliado.ACTIVO);
		when(repositorio.buscarPorId(1L)).thenReturn(Optional.of(existente));
		when(repositorio.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

		Afiliado resultado = servicio.inactivar(1L);

		assertThat(resultado.getEstado()).isEqualTo(EstadoAfiliado.INACTIVO);
		verify(repositorio).guardar(existente);
	}

	@Test
	void lanzaExcepcionSiElAfiliadoNoExiste() {
		when(repositorio.buscarPorId(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servicio.inactivar(1L)).isInstanceOf(AfiliadoNoEncontradoException.class);
		verify(repositorio, never()).guardar(any());
	}

	@Test
	void rn4NoInactivaDosVeces() {
		when(repositorio.buscarPorId(1L)).thenReturn(Optional.of(afiliado(EstadoAfiliado.INACTIVO)));

		assertThatThrownBy(() -> servicio.inactivar(1L)).isInstanceOf(AfiliadoInactivoException.class);
		verify(repositorio, never()).guardar(any());
	}

	@Test
	void rechazaIdNulo() {
		assertThatThrownBy(() -> servicio.inactivar(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void exigeRepositorio() {
		assertThatThrownBy(() -> new InactivarAfiliadoService(null)).isInstanceOf(NullPointerException.class);
	}
}
