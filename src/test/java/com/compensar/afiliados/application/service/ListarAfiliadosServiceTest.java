package com.compensar.afiliados.application.service;

import com.compensar.afiliados.application.port.out.AfiliadoRepositoryPort;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarAfiliadosServiceTest {

	@Mock
	private AfiliadoRepositoryPort repositorio;

	@InjectMocks
	private ListarAfiliadosService servicio;

	@Test
	void devuelveLosAfiliadosDelRepositorio() {
		Afiliado afiliado = Afiliado.reconstituir(1L, new Documento(TipoDocumento.CC, "123456"), "Ana", "Pérez",
				new Email("ana@correo.com"), new BigDecimal("2000000"), Categoria.A, EstadoAfiliado.ACTIVO);
		when(repositorio.listar()).thenReturn(List.of(afiliado));

		assertThat(servicio.listar()).containsExactly(afiliado);
	}

	@Test
	void devuelveListaVaciaSiNoHayAfiliados() {
		when(repositorio.listar()).thenReturn(List.of());

		assertThat(servicio.listar()).isEmpty();
	}

	@Test
	void laListaDevueltaNoSePuedeModificar() {
		when(repositorio.listar()).thenReturn(new java.util.ArrayList<>());

		List<Afiliado> resultado = servicio.listar();

		assertThatThrownBy(() -> resultado.add(null)).isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void exigeRepositorio() {
		assertThatThrownBy(() -> new ListarAfiliadosService(null)).isInstanceOf(NullPointerException.class);
	}
}
