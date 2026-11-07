package com.compensar.afiliados.infrastructure.security;

import com.compensar.afiliados.application.port.in.ActualizarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.ConsultarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.InactivarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.ListarAfiliadosUseCase;
import com.compensar.afiliados.application.port.in.RegistrarAfiliadoUseCase;
import com.compensar.afiliados.domain.model.Afiliado;
import com.compensar.afiliados.domain.model.Categoria;
import com.compensar.afiliados.domain.model.Documento;
import com.compensar.afiliados.domain.model.Email;
import com.compensar.afiliados.domain.model.EstadoAfiliado;
import com.compensar.afiliados.domain.model.TipoDocumento;
import com.compensar.afiliados.infrastructure.adapter.in.web.AfiliadoController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Matriz de autorización: quién puede hacer qué. Usa el convertidor real del claim "roles".
 */
@WebMvcTest(AfiliadoController.class)
@Import(SeguridadConfig.class)
@ExtendWith(OutputCaptureExtension.class)
class SeguridadWebTest {

	private static final String RUTA = "/api/v1/afiliados";
	private static final String REGISTRO = """
			{"tipoDocumento":"CC","numeroDocumento":"1023456789","nombres":"Ana","apellidos":"Pérez",
			 "email":"ana.perez@correo.com","salario":3500000}""";
	private static final String ACTUALIZACION = """
			{"nombres":"Ana","apellidos":"Pérez","email":"ana.perez@correo.com","salario":3500000}""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JwtDecoder jwtDecoder;
	@MockitoBean
	private RegistrarAfiliadoUseCase registrar;
	@MockitoBean
	private ConsultarAfiliadoUseCase consultar;
	@MockitoBean
	private ListarAfiliadosUseCase listar;
	@MockitoBean
	private ActualizarAfiliadoUseCase actualizar;
	@MockitoBean
	private InactivarAfiliadoUseCase inactivar;

	/** Token con el claim "roles", convertido a autoridades con el mismo convertidor que producción. */
	private static JwtRequestPostProcessor token(String... roles) {
		return jwt().jwt(j -> j.subject("usuario-prueba").claim("roles", List.of(roles)))
				.authorities(SeguridadConfig.convertidorDeRoles());
	}

	private static Afiliado afiliado() {
		return Afiliado.reconstituir(1L, new Documento(TipoDocumento.CC, "1023456789"), "Ana", "Pérez",
				new Email("ana.perez@correo.com"), new BigDecimal("3500000"), Categoria.A, EstadoAfiliado.ACTIVO);
	}

	// ---------- 401: sin token o token inválido ----------

	@Test
	void sinTokenDevuelve401ConProblemDetailYWwwAuthenticate() throws Exception {
		mockMvc.perform(get(RUTA))
				.andExpect(status().isUnauthorized())
				// RFC 6750 + RFC 9728: Spring Security 7 agrega resource_metadata para que el cliente descubra
				// cómo obtener un token (GET /.well-known/oauth-protected-resource).
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")))
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("resource_metadata=")))
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.type").value("urn:afiliados:problema:no-autenticado"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.instance").value(RUTA));

		verifyNoInteractions(listar);
	}

	@Test
	void tokenInvalidoDevuelve401SinExplicarElMotivoEnElCuerpo(CapturedOutput salida) throws Exception {
		when(jwtDecoder.decode("token-falso")).thenThrow(new BadJwtException("Signed JWT rejected: Invalid signature"));

		mockMvc.perform(get(RUTA).header(HttpHeaders.AUTHORIZATION, "Bearer token-falso"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("invalid_token")))
				.andExpect(jsonPath("$.detail").value("Se requiere un token Bearer válido"));

		assertThat(salida.getAll()).contains("401 no autenticado").doesNotContain("token-falso");
		verifyNoInteractions(listar);
	}

	@Test
	void unaRutaNoListadaTambienExigeToken() throws Exception {
		// Sin perfil dev, el emisor de tokens no existe y su ruta cae en la regla general.
		mockMvc.perform(post("/dev/token")).andExpect(status().isUnauthorized());
	}

	// ---------- CONSULTA: solo lectura ----------

	@Test
	void consultaPuedeListarYConsultar() throws Exception {
		when(listar.listar()).thenReturn(List.of(afiliado()));
		when(consultar.consultar(1L)).thenReturn(afiliado());

		mockMvc.perform(get(RUTA).with(token("CONSULTA"))).andExpect(status().isOk());
		mockMvc.perform(get(RUTA + "/1").with(token("CONSULTA"))).andExpect(status().isOk());
	}

	@Test
	void consultaNoPuedeRegistrar(CapturedOutput salida) throws Exception {
		mockMvc.perform(post(RUTA).with(token("CONSULTA")).contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
				.andExpect(status().isForbidden())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.type").value("urn:afiliados:problema:acceso-denegado"))
				.andExpect(jsonPath("$.status").value(403));

		assertThat(salida.getAll()).contains("403 acceso denegado metodo=POST", "usuario=usuario-prueba");
		verifyNoInteractions(registrar);
	}

	@Test
	void consultaNoPuedeActualizarNiInactivar() throws Exception {
		mockMvc.perform(put(RUTA + "/1").with(token("CONSULTA")).contentType(MediaType.APPLICATION_JSON).content(ACTUALIZACION))
				.andExpect(status().isForbidden());
		mockMvc.perform(patch(RUTA + "/1/inactivar").with(token("CONSULTA")))
				.andExpect(status().isForbidden());

		verifyNoInteractions(actualizar, inactivar);
	}

	// ---------- ADMIN: todo ----------

	@Test
	void adminPuedeRegistrarActualizarEInactivar() throws Exception {
		when(registrar.registrar(any())).thenReturn(afiliado());
		when(actualizar.actualizar(any(), any())).thenReturn(afiliado());
		when(inactivar.inactivar(1L)).thenReturn(afiliado());

		mockMvc.perform(post(RUTA).with(token("ADMIN")).contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
				.andExpect(status().isCreated());
		mockMvc.perform(put(RUTA + "/1").with(token("ADMIN")).contentType(MediaType.APPLICATION_JSON).content(ACTUALIZACION))
				.andExpect(status().isOk());
		mockMvc.perform(patch(RUTA + "/1/inactivar").with(token("ADMIN")))
				.andExpect(status().isOk());
	}

	@Test
	void adminTambienPuedeLeer() throws Exception {
		when(listar.listar()).thenReturn(List.of());

		mockMvc.perform(get(RUTA).with(token("ADMIN"))).andExpect(status().isOk());
	}

	// ---------- Tokens válidos sin un rol reconocido ----------

	@Test
	void unTokenSinRolesORolDesconocidoNoPuedeNiLeer() throws Exception {
		mockMvc.perform(get(RUTA).with(token())).andExpect(status().isForbidden());
		mockMvc.perform(get(RUTA).with(token("SUPERUSUARIO"))).andExpect(status().isForbidden());
		// El prefijo lo agrega la API: un claim "ROLE_ADMIN" no se convierte en ADMIN.
		mockMvc.perform(get(RUTA).with(token("ROLE_ADMIN"))).andExpect(status().isForbidden());

		verifyNoInteractions(listar);
	}

	// ---------- Cabeceras y estado ----------

	@Test
	void respondeConCabecerasDeSeguridadYSinSesion() throws Exception {
		when(listar.listar()).thenReturn(List.of());

		mockMvc.perform(get(RUTA).secure(true).with(token("CONSULTA")))
				.andExpect(status().isOk())
				.andExpect(header().string("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains"))
				.andExpect(header().string("X-Content-Type-Options", "nosniff"))
				.andExpect(header().string("X-Frame-Options", "DENY"))
				.andExpect(header().string("Referrer-Policy", "no-referrer"))
				.andExpect(header().string("Content-Security-Policy", "frame-ancestors 'none'"))
				.andExpect(header().string("Cross-Origin-Resource-Policy", "same-origin"))
				.andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
	}

	@Test
	void hstsSoloSeEnviaPorHttps() throws Exception {
		// En HTTP plano HSTS no tiene efecto (un atacante en la red podría quitarla); por eso Spring no la envía.
		mockMvc.perform(get(RUTA).with(token("CONSULTA")))
				.andExpect(header().doesNotExist("Strict-Transport-Security"))
				.andExpect(header().string("X-Content-Type-Options", "nosniff"));
	}
}
