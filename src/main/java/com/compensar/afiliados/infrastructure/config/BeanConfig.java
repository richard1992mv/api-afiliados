package com.compensar.afiliados.infrastructure.config;

import com.compensar.afiliados.application.port.in.ActualizarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.ConsultarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.InactivarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.ListarAfiliadosUseCase;
import com.compensar.afiliados.application.port.in.RegistrarAfiliadoUseCase;
import com.compensar.afiliados.application.port.out.AfiliadoRepositoryPort;
import com.compensar.afiliados.application.service.ActualizarAfiliadoService;
import com.compensar.afiliados.application.service.ConsultarAfiliadoService;
import com.compensar.afiliados.application.service.InactivarAfiliadoService;
import com.compensar.afiliados.application.service.ListarAfiliadosService;
import com.compensar.afiliados.application.service.RegistrarAfiliadoService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los servicios de aplicación como beans. Así la capa application no conoce Spring
 * (sin @Service): la infraestructura decide cómo se construye y conecta el núcleo.
 */
@Configuration
@EnableConfigurationProperties(AfiliadosProperties.class)
public class BeanConfig {

	@Bean
	RegistrarAfiliadoUseCase registrarAfiliadoUseCase(AfiliadoRepositoryPort repositorio,
			AfiliadosProperties propiedades) {
		return new RegistrarAfiliadoService(repositorio, propiedades.smmlv());
	}

	@Bean
	ConsultarAfiliadoUseCase consultarAfiliadoUseCase(AfiliadoRepositoryPort repositorio) {
		return new ConsultarAfiliadoService(repositorio);
	}

	@Bean
	ListarAfiliadosUseCase listarAfiliadosUseCase(AfiliadoRepositoryPort repositorio) {
		return new ListarAfiliadosService(repositorio);
	}

	@Bean
	ActualizarAfiliadoUseCase actualizarAfiliadoUseCase(AfiliadoRepositoryPort repositorio,
			AfiliadosProperties propiedades) {
		return new ActualizarAfiliadoService(repositorio, propiedades.smmlv());
	}

	@Bean
	InactivarAfiliadoUseCase inactivarAfiliadoUseCase(AfiliadoRepositoryPort repositorio) {
		return new InactivarAfiliadoService(repositorio);
	}
}
