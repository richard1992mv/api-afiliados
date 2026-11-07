package com.compensar.afiliados.infrastructure.security.dev;

import com.compensar.afiliados.infrastructure.security.JwtProperties;
import com.compensar.afiliados.infrastructure.security.Rol;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * SOLO perfil "dev": genera JWT de prueba firmados con JWT_SECRET para probar la API desde Swagger o curl.
 */
@RestController
@Profile("dev")
@Tag(name = "Desarrollo", description = "Solo existe con el perfil dev. Nunca habilitar en producción.")
public class TokenDesarrolloController {

	static final String RUTA = "/dev/token";
	static final Duration VIGENCIA = Duration.ofHours(1);

	private static final Logger log = LoggerFactory.getLogger(TokenDesarrolloController.class);

	private final JwtEncoder encoder;
	private final String emisor;

	TokenDesarrolloController(JwtEncoder encoder, JwtProperties propiedades) {
		this.encoder = encoder;
		this.emisor = propiedades.emisor();
		log.warn("Perfil dev activo: {} emite tokens de prueba. No usar este perfil en producción.", RUTA);
	}

	@PostMapping(RUTA)
	@SecurityRequirements
	@Operation(summary = "Genera un token de prueba (solo perfil dev)",
			description = "Copie el valor de 'token' y péguelo en el botón Authorize.")
	TokenDesarrolloResponse generar(
			@RequestParam(defaultValue = "ADMIN") List<Rol> roles,
			@RequestParam(defaultValue = "usuario-dev")
			@Pattern(regexp = "[A-Za-z0-9._-]{1,64}", message = "solo letras, números, '.', '_' o '-' (máximo 64)")
			String sujeto) {
		Instant ahora = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(emisor)
				.subject(sujeto)
				.issuedAt(ahora)
				.expiresAt(ahora.plus(VIGENCIA))
				.claim("roles", roles.stream().map(Rol::name).distinct().toList())
				.build();
		JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = encoder.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
		log.info("Token de prueba emitido sujeto={} roles={}", sujeto, roles);
		return new TokenDesarrolloResponse(token, "Bearer", VIGENCIA.toSeconds());
	}

	record TokenDesarrolloResponse(String token, String tipo, long expiraEnSegundos) {
	}
}
