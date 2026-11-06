package com.compensar.afiliados.application.service;

import com.compensar.afiliados.application.port.in.RegistrarAfiliadoComando;
import com.compensar.afiliados.application.port.out.AfiliadoRepositoryPort;
import com.compensar.afiliados.domain.exception.DocumentoDuplicadoException;
import com.compensar.afiliados.domain.model.Afiliado;
import com.compensar.afiliados.domain.model.Categoria;
import com.compensar.afiliados.domain.model.Documento;
import com.compensar.afiliados.domain.model.EstadoAfiliado;
import com.compensar.afiliados.domain.model.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarAfiliadoServiceTest {

	private static final BigDecimal SMMLV = new BigDecimal("1423500");

	@Mock
	private AfiliadoRepositoryPort repositorio;

	private RegistrarAfiliadoService servicio;

	@BeforeEach
	void setUp() {
		servicio = new RegistrarAfiliadoService(repositorio, SMMLV);
	}

	private static RegistrarAfiliadoComando comando(String salario) {
		return new RegistrarAfiliadoComando(TipoDocumento.CC, "1023456789", "Ana", "Pérez",
				"Ana@Correo.com", new BigDecimal(salario));
	}

	@Test
	void registraUnAfiliadoActivoConSuCategoria() {
		when(repositorio.existePorDocumento(any())).thenReturn(false);
		when(repositorio.guardar(any())).thenAnswer(inv -> {
			Afiliado a = inv.getArgument(0);
			return Afiliado.reconstituir(10L, a.getDocumento(), a.getNombres(), a.getApellidos(), a.getEmail(),
					a.getSalario(), a.getCategoria(), a.getEstado());
		});

		Afiliado resultado = servicio.registrar(comando("2847000.01"));

		assertThat(resultado.getId()).isEqualTo(10L);
		assertThat(resultado.getCategoria()).isEqualTo(Categoria.B);
		assertThat(resultado.getEstado()).isEqualTo(EstadoAfiliado.ACTIVO);
		assertThat(resultado.getEmail().valor()).isEqualTo("ana@correo.com");

		ArgumentCaptor<Afiliado> guardado = ArgumentCaptor.forClass(Afiliado.class);
		verify(repositorio).guardar(guardado.capture());
		assertThat(guardado.getValue().getId()).isNull();
		verify(repositorio).existePorDocumento(new Documento(TipoDocumento.CC, "1023456789"));
	}

	@Test
	void rn1RechazaUnDocumentoDuplicadoSinGuardar() {
		when(repositorio.existePorDocumento(any())).thenReturn(true);

		assertThatThrownBy(() -> servicio.registrar(comando("2000000")))
				.isInstanceOf(DocumentoDuplicadoException.class)
				.hasMessageNotContaining("1023456789")
				.satisfies(e -> assertThat(((DocumentoDuplicadoException) e).getDocumento().numero())
						.isEqualTo("1023456789"));

		verify(repositorio, never()).guardar(any());
	}

	@Test
	void validaLaEntradaAntesDeConsultarElRepositorio() {
		RegistrarAfiliadoComando invalido = new RegistrarAfiliadoComando(TipoDocumento.CC, "1023456789",
				"Ana", "Pérez", "correo-invalido", new BigDecimal("2000000"));

		assertThatThrownBy(() -> servicio.registrar(invalido))
				.isInstanceOf(IllegalArgumentException.class);

		verifyNoInteractions(repositorio);
	}

	@Test
	void rechazaComandoNulo() {
		assertThatThrownBy(() -> servicio.registrar(null)).isInstanceOf(NullPointerException.class);
		verifyNoInteractions(repositorio);
	}

	@Test
	void exigeRepositorioYSmmlvValidosAlConstruirse() {
		assertThatThrownBy(() -> new RegistrarAfiliadoService(null, SMMLV))
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> new RegistrarAfiliadoService(repositorio, BigDecimal.ZERO))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
