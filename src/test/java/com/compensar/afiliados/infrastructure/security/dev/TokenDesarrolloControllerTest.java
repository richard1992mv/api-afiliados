package com.compensar.afiliados.infrastructure.security.dev;

import com.compensar.afiliados.infrastructure.security.JwtConfig;
import com.compensar.afiliados.infrastructure.security.SeguridadConfig;
import com.compensar.afiliados.infrastructure.security.TokensDePrueba;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TokenDesarrolloController.class)
@ActiveProfiles("dev")
@Import({SeguridadConfig.class, JwtConfig.class, TokenDesarrolloConfig.class})
@TestPropertySource(properties = {
		"afiliados.seguridad.jwt.secreto=" + TokensDePrueba.SECRETO,
		"afiliados.seguridad.jwt.emisor=" + TokensDePrueba.EMISOR})
class TokenDesarrolloControllerTest {

	@Autowired
	private MockMvc mockMvc;

	/** El decoder real de la API: si acepta el token, la API también lo aceptará. */
	@Autowired
	private JwtDecoder decoder;

	@Test
	void emiteUnTokenQueLaPropiaApiAcepta() throws Exception {
		String respuesta = mockMvc.perform(post("/dev/token").param("roles", "CONSULTA").param("sujeto", "ana.perez"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tipo").value("Bearer"))
				.andExpect(jsonPath("$.expiraEnSegundos").value(3600))
				.andReturn().getResponse().getContentAsString();

		Jwt jwt = decoder.decode(JsonPath.read(respuesta, "$.token"));

		assertThat(jwt.getSubject()).isEqualTo("ana.perez");
		assertThat(jwt.getClaimAsString("iss")).isEqualTo(TokensDePrueba.EMISOR);
		assertThat(jwt.getClaimAsStringList("roles")).containsExactly("CONSULTA");
		assertThat(jwt.getExpiresAt()).isBetween(Instant.now().plus(Duration.ofMinutes(59)), Instant.now().plus(Duration.ofMinutes(61)));
	}

	@Test
	void porDefectoEmiteUnTokenAdmin() throws Exception {
		String respuesta = mockMvc.perform(post("/dev/token"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		assertThat(decoder.decode(JsonPath.read(respuesta, "$.token")).getClaimAsStringList("roles")).containsExactly("ADMIN");
	}

	@Test
	void soloEmiteRolesConocidos() throws Exception {
		mockMvc.perform(post("/dev/token").param("roles", "SUPERUSUARIO")).andExpect(status().isBadRequest());
	}

	@Test
	void validaElSujeto() throws Exception {
		mockMvc.perform(post("/dev/token").param("sujeto", "<script>")).andExpect(status().isBadRequest());
	}

	@Test
	void laCadenaDevSoloPermiteElPostDelEmisor() throws Exception {
		mockMvc.perform(get("/dev/token")).andExpect(status().isForbidden());
		mockMvc.perform(get("/dev/otra-cosa")).andExpect(status().isForbidden());
	}
}
