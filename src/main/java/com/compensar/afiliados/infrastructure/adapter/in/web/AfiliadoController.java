package com.compensar.afiliados.infrastructure.adapter.in.web;

import com.compensar.afiliados.application.port.in.ActualizarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.ConsultarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.InactivarAfiliadoUseCase;
import com.compensar.afiliados.application.port.in.ListarAfiliadosUseCase;
import com.compensar.afiliados.application.port.in.RegistrarAfiliadoUseCase;
import com.compensar.afiliados.domain.model.Afiliado;
import com.compensar.afiliados.infrastructure.logging.Enmascarador;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Adaptador de entrada REST. Solo traduce HTTP <-> casos de uso: no contiene reglas de negocio.
 */
@RestController
@RequestMapping(AfiliadoController.RUTA)
@Tag(name = "Afiliados", description = "Gestión de afiliados de la caja de compensación")
public class AfiliadoController {

	static final String RUTA = "/api/v1/afiliados";

	private static final Logger log = LoggerFactory.getLogger(AfiliadoController.class);

	private final RegistrarAfiliadoUseCase registrarAfiliado;
	private final ConsultarAfiliadoUseCase consultarAfiliado;
	private final ListarAfiliadosUseCase listarAfiliados;
	private final ActualizarAfiliadoUseCase actualizarAfiliado;
	private final InactivarAfiliadoUseCase inactivarAfiliado;

	public AfiliadoController(RegistrarAfiliadoUseCase registrarAfiliado, ConsultarAfiliadoUseCase consultarAfiliado,
			ListarAfiliadosUseCase listarAfiliados, ActualizarAfiliadoUseCase actualizarAfiliado,
			InactivarAfiliadoUseCase inactivarAfiliado) {
		this.registrarAfiliado = registrarAfiliado;
		this.consultarAfiliado = consultarAfiliado;
		this.listarAfiliados = listarAfiliados;
		this.actualizarAfiliado = actualizarAfiliado;
		this.inactivarAfiliado = inactivarAfiliado;
	}

	@PostMapping
	@Operation(summary = "Registrar un afiliado", description = "RN1 documento único, RN2 categoría, RN4 queda ACTIVO")
	@ApiResponse(responseCode = "201", description = "Afiliado creado")
	@ApiResponse(responseCode = "400", description = "Datos inválidos")
	@ApiResponse(responseCode = "409", description = "Documento duplicado")
	public ResponseEntity<AfiliadoResponse> registrar(@Valid @RequestBody RegistrarAfiliadoRequest request) {
		log.info("Registrando afiliado tipoDocumento={} documento={}",
				request.tipoDocumento(), Enmascarador.documento(request.numeroDocumento()));
		Afiliado afiliado = registrarAfiliado.registrar(AfiliadoWebMapper.aComando(request));
		log.info("Afiliado registrado id={} categoria={}", afiliado.getId(), afiliado.getCategoria());
		// Location relativo: no se construye con el Host de la petición (evita Host header injection).
		return ResponseEntity.created(URI.create(RUTA + "/" + afiliado.getId()))
				.body(AfiliadoWebMapper.aResponse(afiliado));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar un afiliado por id")
	@ApiResponse(responseCode = "200", description = "Afiliado encontrado")
	@ApiResponse(responseCode = "404", description = "No existe")
	public AfiliadoResponse consultar(@PathVariable @Positive Long id) {
		return AfiliadoWebMapper.aResponse(consultarAfiliado.consultar(id));
	}

	@GetMapping
	@Operation(summary = "Listar afiliados")
	public List<AfiliadoResponse> listar() {
		return listarAfiliados.listar().stream().map(AfiliadoWebMapper::aResponse).toList();
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar un afiliado", description = "RN2 recalcula la categoría; RN4 no aplica a inactivos")
	@ApiResponse(responseCode = "200", description = "Afiliado actualizado")
	@ApiResponse(responseCode = "400", description = "Datos inválidos")
	@ApiResponse(responseCode = "404", description = "No existe")
	@ApiResponse(responseCode = "422", description = "Afiliado inactivo")
	public AfiliadoResponse actualizar(@PathVariable @Positive Long id,
			@Valid @RequestBody ActualizarAfiliadoRequest request) {
		log.info("Actualizando afiliado id={} email={}", id, Enmascarador.email(request.email()));
		return AfiliadoWebMapper.aResponse(actualizarAfiliado.actualizar(id, AfiliadoWebMapper.aComando(request)));
	}

	@PatchMapping("/{id}/inactivar")
	@Operation(summary = "Inactivar un afiliado", description = "Cambio de estado lógico: el registro se conserva")
	@ApiResponse(responseCode = "200", description = "Afiliado inactivado")
	@ApiResponse(responseCode = "404", description = "No existe")
	@ApiResponse(responseCode = "422", description = "Ya estaba inactivo")
	public AfiliadoResponse inactivar(@PathVariable @Positive Long id) {
		log.info("Inactivando afiliado id={}", id);
		return AfiliadoWebMapper.aResponse(inactivarAfiliado.inactivar(id));
	}
}
