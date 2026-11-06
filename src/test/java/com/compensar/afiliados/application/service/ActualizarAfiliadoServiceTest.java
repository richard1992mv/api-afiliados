package com.compensar.afiliados.application.service;

import com.compensar.afiliados.application.port.in.ActualizarAfiliadoComando;
import com.compensar.afiliados.application.port.out.AfiliadoRepositoryPort;
import com.compensar.afiliados.domain.exception.AfiliadoInactivoException;
import com.compensar.afiliados.domain.exception.AfiliadoNoEncontradoException;
import com.compensar.afiliados.domain.model.Afiliado;
import com.compensar.afiliados.domain.model.Categoria;
import com.compensar.afiliados.domain.model.Documento;
import com.compensar.afiliados.domain.model.Email;
import com.compensar.afiliados.domain.model.EstadoAfiliado;
import com.compensar.afiliados.domain.model.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ActualizarAfiliadoServiceTest {

	private static final BigDecimal SMMLV = new BigDecimal("1423500");

	@Mock
	private AfiliadoRepositoryPort repositorio;

	private ActualizarAfiliadoService servicio;

	@BeforeEach
	void setUp() {
		servicio = new ActualizarAfiliadoService(repositorio, SMMLV);
	}

	private static Afiliado afiliado(EstadoAfiliado estado) {
		return Afiliado.reconstituir(1L, new Documento(TipoDocumento.CC, "123456"), "Ana", "Pérez",
				new Email("ana@correo.com"), new BigDecimal("2000000"), Categoria.A, estado);
	}

	private static ActualizarAfiliadoComando comando(String salario) {
		return new ActualizarAfiliadoComando("Ana María", "Pérez Ruiz", "ana.maria@correo.com", new BigDecimal(salario));
	}

	@Test
	void actualizaYRecalculaLaCategoria() {
		Afiliado existente = afiliado(EstadoAfiliado.ACTIVO);
		when(repositorio.buscarPorId(1L)).thenReturn(Optional.of(existente));
		when(repositorio.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

		Afiliado resultado = servicio.actualizar(1L, comando("5694000"));

		assertThat(resultado.getNombres()).isEqualTo("Ana María");
		assertThat(resultado.getEmail().valor()).isEqualTo("ana.maria@correo.com");
		assertThat(resultado.getCategoria()).isEqualTo(Categoria.B);
		verify(repositorio).guardar(existente);
	}

	@Test
	void lanzaExcepcionSiElAfiliadoNoExiste() {
		when(repositorio.buscarPorId(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servicio.actualizar(1L, comando("2000000")))
				.isInstanceOf(AfiliadoNoEncontradoException.class);
		verify(repositorio, never()).guardar(any());
	}

	@Test
	void rn4NoActualizaUnAfiliadoInactivo() {
		when(repositorio.buscarPorId(1L)).thenReturn(Optional.of(afiliado(EstadoAfiliado.INACTIVO)));

		assertThatThrownBy(() -> servicio.actualizar(1L, comando("2000000")))
				.isInstanceOf(AfiliadoInactivoException.class);
		verify(repositorio, never()).guardar(any());
	}

	@Test
	void rechazaIdOComandoNulos() {
		assertThatThrownBy(() -> servicio.actualizar(null, comando("2000000")))
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> servicio.actualizar(1L, null))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	void exigeRepositorioYSmmlvValidosAlConstruirse() {
		assertThatThrownBy(() -> new ActualizarAfiliadoService(null, SMMLV))
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> new ActualizarAfiliadoService(repositorio, null))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
