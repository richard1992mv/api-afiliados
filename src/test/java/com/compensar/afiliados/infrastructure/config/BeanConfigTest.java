package com.compensar.afiliados.infrastructure.config;

import com.compensar.afiliados.application.port.out.AfiliadoRepositoryPort;
import com.compensar.afiliados.application.service.ActualizarAfiliadoService;
import com.compensar.afiliados.application.service.ConsultarAfiliadoService;
import com.compensar.afiliados.application.service.InactivarAfiliadoService;
import com.compensar.afiliados.application.service.ListarAfiliadosService;
import com.compensar.afiliados.application.service.RegistrarAfiliadoService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class BeanConfigTest {

	private final BeanConfig config = new BeanConfig();
	private final AfiliadoRepositoryPort repositorio = mock(AfiliadoRepositoryPort.class);
	private final AfiliadosProperties propiedades = new AfiliadosProperties(new BigDecimal("1750905"));

	@Test
	void creaUnServicioPorCasoDeUso() {
		assertThat(config.registrarAfiliadoUseCase(repositorio, propiedades)).isInstanceOf(RegistrarAfiliadoService.class);
		assertThat(config.consultarAfiliadoUseCase(repositorio)).isInstanceOf(ConsultarAfiliadoService.class);
		assertThat(config.listarAfiliadosUseCase(repositorio)).isInstanceOf(ListarAfiliadosService.class);
		assertThat(config.actualizarAfiliadoUseCase(repositorio, propiedades)).isInstanceOf(ActualizarAfiliadoService.class);
		assertThat(config.inactivarAfiliadoUseCase(repositorio)).isInstanceOf(InactivarAfiliadoService.class);
	}

	@Test
	void unSmmlvInvalidoImpideCrearLosServicios() {
		AfiliadosProperties invalidas = new AfiliadosProperties(BigDecimal.ZERO);

		assertThatThrownBy(() -> config.registrarAfiliadoUseCase(repositorio, invalidas))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void openApiTieneTituloYVersion() {
		var openApi = new OpenApiConfig().afiliadosOpenApi();

		assertThat(openApi.getInfo().getTitle()).isEqualTo("API Afiliados");
		assertThat(openApi.getInfo().getVersion()).isEqualTo("v1");
	}
}
