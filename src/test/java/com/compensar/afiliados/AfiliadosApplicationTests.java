package com.compensar.afiliados;

import com.compensar.afiliados.application.port.in.ActualizarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.ConsultarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.InactivarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.ListarAfiliadosUseCase;
import com.compensar.afiliados.application.port.in.RegistrarAfiliadoUseCase;
import com.compensar.afiliados.application.port.out.AfiliadoRepositoryPort;
import com.compensar.afiliados.infrastructure.config.AfiliadosProperties;
import com.compensar.afiliados.infrastructure.security.TokensDePrueba;
import com.compensar.afiliados.infrastructure.security.dev.TokenDesarrolloController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Arranque completo de la aplicación contra MySQL real (Testcontainers):
 * valida la migración Flyway, el mapeo JPA (ddl-auto=validate), BeanConfig, las propiedades
 * y la seguridad de punta a punta con tokens firmados de verdad.
 */
@SpringBootTest(properties = "afiliados.seguridad.jwt.secreto=" + TokensDePrueba.SECRETO)
@AutoConfigureMockMvc
@Import(MySqlTestcontainersConfig.class)
class AfiliadosApplicationTests {

	private static final String RUTA = "/api/v1/afiliados";

	@Autowired
	private ApplicationContext contexto;

	@Autowired
	private MockMvc mockMvc;

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

	@Test
	void sinPerfilDevNoExisteElEmisorDeTokens() throws Exception {
		assertThat(contexto.getBeanNamesForType(TokenDesarrolloController.class)).isEmpty();
		assertThat(contexto.getBeanNamesForType(JwtEncoder.class)).isEmpty();
		mockMvc.perform(post("/dev/token")).andExpect(status().isUnauthorized());
	}

	@Test
	void healthYDocumentacionSonPublicos() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components.securitySchemes.bearer-jwt.scheme").value("bearer"))
				.andExpect(jsonPath("$.components.securitySchemes.bearer-jwt.bearerFormat").value("JWT"));
	}

	@Test
	void swaggerUiCargaSusArchivos() throws Exception {
		// Regresión detectada por DAST: al fijar swagger-ui en el pom, springdoc seguía buscando los
		// archivos de su versión interna y /swagger-ui/index.html devolvía 404 (el redirect sí funcionaba).
		mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
		mockMvc.perform(get("/swagger-ui/swagger-ui-bundle.js")).andExpect(status().isOk());
	}

	@Test
	void losMetadatosDelRecursoProtegidoSoloPublicanComoEnviarElToken() throws Exception {
		// RFC 9728: público por diseño. Fija qué expone, para que un cambio quede visible en un PR.
		mockMvc.perform(get("/.well-known/oauth-protected-resource"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resource").value("http://localhost"))
				.andExpect(jsonPath("$.bearer_methods_supported[0]").value("header"))
				.andExpect(jsonPath("$.authorization_servers").doesNotExist());
	}

	@Test
	void unTokenFirmadoConLaLlaveDeLaApiPermiteLeer() throws Exception {
		mockMvc.perform(get(RUTA).header(HttpHeaders.AUTHORIZATION, "Bearer " + TokensDePrueba.conRoles("CONSULTA")))
				.andExpect(status().isOk());
	}

	@Test
	void unTokenFirmadoPorUnTerceroEsRechazado() throws Exception {
		String falsificado = TokensDePrueba.firmar(TokensDePrueba.OTRO_SECRETO,
				claims -> claims.claim("roles", List.of("ADMIN")));

		mockMvc.perform(get(RUTA).header(HttpHeaders.AUTHORIZATION, "Bearer " + falsificado))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.type").value("urn:afiliados:problema:no-autenticado"));
	}
}
