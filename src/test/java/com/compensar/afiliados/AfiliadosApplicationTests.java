package com.compensar.afiliados;

import com.compensar.afiliados.application.port.in.ActualizarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.ConsultarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.InactivarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.ListarAfiliadosUseCase;
import com.compensar.afiliados.application.port.in.RegistrarAfiliadoUseCase;
import com.compensar.afiliados.application.port.out.AfiliadoRepositoryPort;
import com.compensar.afiliados.infrastructure.config.AfiliadosProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Arranque completo de la aplicación contra MySQL real (Testcontainers):
 * valida la migración Flyway, el mapeo JPA (ddl-auto=validate), BeanConfig y las propiedades.
 */
@SpringBootTest
@Import(MySqlTestcontainersConfig.class)
class AfiliadosApplicationTests {

	@Autowired
	private ApplicationContext contexto;

	@Test
	void contextLoads() {
		assertThat(contexto.getBean(AfiliadoRepositoryPort.class)).isNotNull();
		assertThat(contexto.getBean(RegistrarAfiliadoUseCase.class)).isNotNull();
		assertThat(contexto.getBean(ConsultarAfiliadoUseCase.class)).isNotNull();
		assertThat(contexto.getBean(ListarAfiliadosUseCase.class)).isNotNull();
		assertThat(contexto.getBean(ActualizarAfiliadoUseCase.class)).isNotNull();
		assertThat(contexto.getBean(InactivarAfiliadoUseCase.class)).isNotNull();
		assertThat(contexto.getBean(AfiliadosProperties.class).smmlv()).isPositive();
	}
}
