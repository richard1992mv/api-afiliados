package com.compensar.afiliados.infrastructure.adapter.in.web;

import com.compensar.afiliados.application.port.in.ActualizarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.ConsultarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.InactivarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.ListarAfiliadosUseCase;
import com.compensar.afiliados.application.port.in.RegistrarAfiliadoComando;
import com.compensar.afiliados.application.port.in.RegistrarAfiliadoUseCase;
import com.compensar.afiliados.domain.exception.AfiliadoInactivoException;
import com.compensar.afiliados.domain.exception.AfiliadoNoEncontradoException;
import com.compensar.afiliados.domain.exception.DatoInvalidoException;
import com.compensar.afiliados.domain.exception.DocumentoDuplicadoException;
import com.compensar.afiliados.domain.model.Afiliado;
import com.compensar.afiliados.domain.model.Categoria;
import com.compensar.afiliados.domain.model.Documento;
import com.compensar.afiliados.domain.model.Email;
import com.compensar.afiliados.domain.model.EstadoAfiliado;
import com.compensar.afiliados.domain.model.TipoDocumento;
import com.compensar.afiliados.infrastructure.security.SeguridadConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AfiliadoController.class)
@Import(SeguridadConfig.class)
@ExtendWith(OutputCaptureExtension.class)
class AfiliadoControllerTest {

	private static final String RUTA = "/api/v1/afiliados";

	private static final String REGISTRO_VALIDO = """
			{"tipoDocumento":"CC","numeroDocumento":"1023456789","nombres":"Ana","apellidos":"Pérez",
			 "email":"ana.perez@correo.com","salario":3500000}""";

	private static final String ACTUALIZACION_VALIDA = """
			{"nombres":"Ana María","apellidos":"Pérez","email":"ana.perez@correo.com","salario":8000000}""";

	@Autowired
	private WebApplicationContext contexto;

	private MockMvc mockMvc;

	/** La seguridad real está activa, pero el decoder no se usa: jwt() inyecta la autenticación directamente. */
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

	/**
	 * Estas pruebas cubren el comportamiento de negocio, por eso todas las peticiones van como ADMIN.
	 * Los casos 401/403 por rol están en SeguridadWebTest.
	 */
	@BeforeEach
	void configurarMockMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup(contexto)
				.apply(springSecurity())
				.defaultRequest(get(RUTA).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
				.build();
	}

	private static Afiliado afiliado(Long id, Categoria categoria, EstadoAfiliado estado) {
		return Afiliado.reconstituir(id, new Documento(TipoDocumento.CC, "1023456789"), "Ana", "Pérez",
				new Email("ana.perez@correo.com"), new BigDecimal("3500000"), categoria, estado);
	}

	// --- POST ---

	@Test
	void registrarDevuelve201ConLocationYElAfiliado() throws Exception {
		when(registrar.registrar(any())).thenReturn(afiliado(1L, Categoria.B, EstadoAfiliado.ACTIVO));

		mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(REGISTRO_VALIDO))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", RUTA + "/1"))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.tipoDocumento").value("CC"))
				.andExpect(jsonPath("$.categoria").value("B"))
				.andExpect(jsonPath("$.estado").value("ACTIVO"));

		ArgumentCaptor<RegistrarAfiliadoComando> comando = ArgumentCaptor.forClass(RegistrarAfiliadoComando.class);
		verify(registrar).registrar(comando.capture());
		assertThat(comando.getValue().numeroDocumento()).isEqualTo("1023456789");
		assertThat(comando.getValue().salario()).isEqualByComparingTo("3500000");
	}

	@Test
	void ley1581LosLogsNoContienenElDocumentoNiElEmailCompletos(CapturedOutput salida) throws Exception {
		when(registrar.registrar(any())).thenReturn(afiliado(1L, Categoria.B, EstadoAfiliado.ACTIVO));
		when(actualizar.actualizar(eq(1L), any())).thenReturn(afiliado(1L, Categoria.C, EstadoAfiliado.ACTIVO));

		mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(REGISTRO_VALIDO));
		mockMvc.perform(put(RUTA + "/1").contentType(MediaType.APPLICATION_JSON).content(ACTUALIZACION_VALIDA));

		assertThat(salida.getAll())
				.contains("******6789", "a***@correo.com")
				.doesNotContain("1023456789", "ana.perez@correo.com");
	}

	@Test
	void registrarConCamposInvalidosDevuelve400ConLosErroresSinRepetirValores() throws Exception {
		String invalido = """
				{"tipoDocumento":"CC","numeroDocumento":"12-34","nombres":"","apellidos":"Pérez",
				 "email":"no-es-email","salario":-5}""";

		mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(invalido))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Datos inválidos"))
				.andExpect(jsonPath("$.errores", hasSize(4)))
				.andExpect(jsonPath("$.errores[*].campo").value(
						org.hamcrest.Matchers.containsInAnyOrder("numeroDocumento", "nombres", "email", "salario")))
				.andExpect(content().string(not(containsString("no-es-email"))));

		verifyNoInteractions(registrar);
	}

	@Test
	void rn3UnTipoDeDocumentoNoPermitidoDevuelve400() throws Exception {
		String conNit = REGISTRO_VALIDO.replace("\"CC\"", "\"NIT\"");

		mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(conNit))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Cuerpo inválido"))
				.andExpect(jsonPath("$.detail").value(containsString("CC, CE, TI, PA")));

		verifyNoInteractions(registrar);
	}

	@Test
	void jsonMalFormadoDevuelve400() throws Exception {
		mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content("{ no es json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Cuerpo inválido"));
	}

	@Test
	void unDatoInvalidoDelDominioDevuelve400() throws Exception {
		when(registrar.registrar(any())).thenThrow(new DatoInvalidoException("El email no tiene un formato válido"));

		mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(REGISTRO_VALIDO))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.type").value("urn:afiliados:problema:dato-invalido"))
				.andExpect(jsonPath("$.detail").value("El email no tiene un formato válido"));
	}

	@Test
	void rn1DocumentoDuplicadoDevuelve409SinElNumero(CapturedOutput salida) throws Exception {
		when(registrar.registrar(any()))
				.thenThrow(new DocumentoDuplicadoException(new Documento(TipoDocumento.CC, "1023456789")));

		mockMvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(REGISTRO_VALIDO))
				.andExpect(status().isConflict())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.type").value("urn:afiliados:problema:documento-duplicado"))
				.andExpect(content().string(not(containsString("1023456789"))));

		assertThat(salida.getAll()).contains("RN1").doesNotContain("1023456789");
	}

	// --- GET ---

	@Test
	void consultarDevuelve200() throws Exception {
		when(consultar.consultar(1L)).thenReturn(afiliado(1L, Categoria.B, EstadoAfiliado.ACTIVO));

		mockMvc.perform(get(RUTA + "/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.numeroDocumento").value("1023456789"))
				.andExpect(jsonPath("$.email").value("ana.perez@correo.com"));
	}

	@Test
	void consultarUnIdInexistenteDevuelve404() throws Exception {
		when(consultar.consultar(99L)).thenThrow(new AfiliadoNoEncontradoException(99L));

		mockMvc.perform(get(RUTA + "/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Afiliado no encontrado"))
				.andExpect(jsonPath("$.instance").value(RUTA + "/99"));
	}

	@Test
	void unIdNoNumericoOMenorQueUnoDevuelve400() throws Exception {
		mockMvc.perform(get(RUTA + "/abc")).andExpect(status().isBadRequest());
		mockMvc.perform(get(RUTA + "/0"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores[0].campo").value("id"));

		verifyNoInteractions(consultar);
	}

	@Test
	void listarDevuelve200ConLosAfiliados() throws Exception {
		when(listar.listar()).thenReturn(List.of(
				afiliado(1L, Categoria.A, EstadoAfiliado.ACTIVO),
				afiliado(2L, Categoria.C, EstadoAfiliado.INACTIVO)));

		mockMvc.perform(get(RUTA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[1].estado").value("INACTIVO"));
	}

	// --- PUT ---

	@Test
	void actualizarDevuelve200ConLaCategoriaRecalculada() throws Exception {
		when(actualizar.actualizar(eq(1L), any())).thenReturn(afiliado(1L, Categoria.C, EstadoAfiliado.ACTIVO));

		mockMvc.perform(put(RUTA + "/1").contentType(MediaType.APPLICATION_JSON).content(ACTUALIZACION_VALIDA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.categoria").value("C"));
	}

	@Test
	void rn4ActualizarUnAfiliadoInactivoDevuelve422() throws Exception {
		when(actualizar.actualizar(eq(1L), any())).thenThrow(new AfiliadoInactivoException(1L));

		mockMvc.perform(put(RUTA + "/1").contentType(MediaType.APPLICATION_JSON).content(ACTUALIZACION_VALIDA))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.type").value("urn:afiliados:problema:afiliado-inactivo"));
	}

	@Test
	void actualizarConCuerpoInvalidoDevuelve400() throws Exception {
		mockMvc.perform(put(RUTA + "/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errores", hasSize(4)));

		verifyNoInteractions(actualizar);
	}

	// --- PATCH ---

	@Test
	void inactivarDevuelve200() throws Exception {
		when(inactivar.inactivar(1L)).thenReturn(afiliado(1L, Categoria.B, EstadoAfiliado.INACTIVO));

		mockMvc.perform(patch(RUTA + "/1/inactivar"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value("INACTIVO"));
	}

	@Test
	void rn4InactivarDosVecesDevuelve422() throws Exception {
		when(inactivar.inactivar(1L)).thenThrow(new AfiliadoInactivoException(1L));

		mockMvc.perform(patch(RUTA + "/1/inactivar")).andExpect(status().isUnprocessableContent());
	}

	// --- 500 ---

	@Test
	void unErrorInesperadoDevuelve500GenericoSinDetallesInternos(CapturedOutput salida) throws Exception {
		when(listar.listar()).thenThrow(new IllegalStateException("conexión rota a jdbc:mysql://10.0.0.5 doc 1023456789"));

		mockMvc.perform(get(RUTA))
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Error interno"))
				.andExpect(jsonPath("$.incidente").isNotEmpty())
				.andExpect(content().string(not(containsString("jdbc"))))
				.andExpect(content().string(not(containsString("1023456789"))))
				.andExpect(content().string(not(containsString("IllegalStateException"))));

		// En nivel INFO/ERROR el log tiene el incidente y el tipo, pero no el mensaje con datos sensibles.
		assertThat(salida.getAll()).contains("IllegalStateException").doesNotContain("1023456789");
	}
}
