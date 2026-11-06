package com.compensar.afiliados.infrastructure.adapter.in.web;

import com.compensar.afiliados.domain.exception.AfiliadoInactivoException;
import com.compensar.afiliados.domain.exception.AfiliadoNoEncontradoException;
import com.compensar.afiliados.domain.exception.DatoInvalidoException;
import com.compensar.afiliados.domain.exception.DocumentoDuplicadoException;
import com.compensar.afiliados.infrastructure.logging.Enmascarador;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Traduce las excepciones a respuestas ProblemDetail (RFC 7807 / RFC 9457).
 * Nunca devuelve trazas ni mensajes internos, ni valores enviados por el cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final String TIPO_BASE = "urn:afiliados:problema:";

	@ExceptionHandler(DatoInvalidoException.class)
	ProblemDetail datoInvalido(DatoInvalidoException e) {
		return problema(HttpStatus.BAD_REQUEST, "dato-invalido", "Datos inválidos", e.getMessage());
	}

	@ExceptionHandler(AfiliadoNoEncontradoException.class)
	ProblemDetail noEncontrado(AfiliadoNoEncontradoException e) {
		return problema(HttpStatus.NOT_FOUND, "afiliado-no-encontrado", "Afiliado no encontrado", e.getMessage());
	}

	@ExceptionHandler(DocumentoDuplicadoException.class)
	ProblemDetail documentoDuplicado(DocumentoDuplicadoException e) {
		if (e.getDocumento() != null) {
			log.warn("RN1: documento duplicado tipoDocumento={} documento={}",
					e.getDocumento().tipo(), Enmascarador.documento(e.getDocumento().numero()));
		}
		return problema(HttpStatus.CONFLICT, "documento-duplicado", "Documento duplicado", e.getMessage());
	}

	@ExceptionHandler(AfiliadoInactivoException.class)
	ProblemDetail afiliadoInactivo(AfiliadoInactivoException e) {
		return problema(HttpStatus.UNPROCESSABLE_CONTENT, "afiliado-inactivo", "Afiliado inactivo", e.getMessage());
	}

	/**
	 * 401: falta el token o es inválido (firma, emisor, vencido). El detalle es genérico a propósito:
	 * explicar por qué falló orienta a quien intenta falsificar tokens. El token nunca se registra.
	 */
	@ExceptionHandler(AuthenticationException.class)
	ProblemDetail noAutenticado(AuthenticationException e, HttpServletRequest peticion) {
		log.warn("401 no autenticado metodo={} ruta={} motivo={}",
				peticion.getMethod(), peticion.getRequestURI(), e.getClass().getSimpleName());
		return problema(HttpStatus.UNAUTHORIZED, "no-autenticado", "No autenticado",
				"Se requiere un token Bearer válido");
	}

	/** 403: el token es válido pero su rol no permite la operación. */
	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail accesoDenegado(AccessDeniedException e, HttpServletRequest peticion) {
		log.warn("403 acceso denegado metodo={} ruta={} usuario={}", peticion.getMethod(),
				peticion.getRequestURI(), peticion.getUserPrincipal() == null ? "-" : peticion.getUserPrincipal().getName());
		return problema(HttpStatus.FORBIDDEN, "acceso-denegado", "Acceso denegado",
				"Su rol no permite realizar esta operación");
	}

	/**
	 * 500 genérico: el cliente solo recibe un identificador de incidente para correlacionar con los logs.
	 * En ERROR se registra el tipo de excepción; la traza completa solo en DEBUG, porque el mensaje de una
	 * excepción inesperada podría contener datos personales.
	 */
	@ExceptionHandler(Exception.class)
	ProblemDetail errorInesperado(Exception e) {
		String incidente = UUID.randomUUID().toString();
		log.error("Error no controlado incidente={} tipo={}", incidente, e.getClass().getName());
		log.debug("Detalle del incidente {}", incidente, e);
		ProblemDetail problema = problema(HttpStatus.INTERNAL_SERVER_ERROR, "error-interno", "Error interno",
				"Ocurrió un error inesperado. Si persiste, reporte el incidente " + incidente);
		problema.setProperty("incidente", incidente);
		return problema;
	}

	/** 400 de Bean Validation sobre el cuerpo: indica qué campo falló y por qué, sin repetir el valor recibido. */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<Map<String, String>> errores = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error(error.getField(), error.getDefaultMessage()))
				.toList();
		return handleExceptionInternal(ex, problemaDeValidacion(errores), headers, status, request);
	}

	/**
	 * 400 cuando aplica la validación de métodos (por ejemplo, {@code @Positive} en el id). En ese caso
	 * Spring reporta aquí también los errores del cuerpo {@code @Valid}, por eso se recorren ambos.
	 */
	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<Map<String, String>> errores = new ArrayList<>();
		for (ParameterValidationResult resultado : ex.getParameterValidationResults()) {
			if (resultado instanceof ParameterErrors erroresDeObjeto) {
				erroresDeObjeto.getFieldErrors()
						.forEach(error -> errores.add(error(error.getField(), error.getDefaultMessage())));
			} else {
				String parametro = Objects.requireNonNullElse(resultado.getMethodParameter().getParameterName(), "parametro");
				resultado.getResolvableErrors()
						.forEach(error -> errores.add(error(parametro, error.getDefaultMessage())));
			}
		}
		return handleExceptionInternal(ex, problemaDeValidacion(errores), headers, status, request);
	}

	/** 400 para JSON mal formado o valores no permitidos (por ejemplo, un tipo de documento fuera de RN3). */
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "cuerpo-invalido", "Cuerpo inválido",
				"El cuerpo no es un JSON válido o tiene valores no permitidos (tipoDocumento: CC, CE, TI, PA)");
		return handleExceptionInternal(ex, problema, headers, status, request);
	}

	private static ProblemDetail problemaDeValidacion(List<Map<String, String>> errores) {
		ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "validacion", "Datos inválidos",
				"La solicitud tiene campos inválidos");
		problema.setProperty("errores", errores);
		return problema;
	}

	private static Map<String, String> error(String campo, String mensaje) {
		return Map.of("campo", campo, "mensaje", Objects.requireNonNullElse(mensaje, "Valor inválido"));
	}

	private static ProblemDetail problema(HttpStatus estado, String tipo, String titulo, String detalle) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
		problema.setType(URI.create(TIPO_BASE + tipo));
		problema.setTitle(titulo);
		return problema;
	}
}
