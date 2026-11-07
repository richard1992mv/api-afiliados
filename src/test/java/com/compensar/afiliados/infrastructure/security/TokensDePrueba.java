package com.compensar.afiliados.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utilidades para firmar JWT reales en las pruebas.
 * La llave es pública a propósito (solo existe en src/test): no protege nada y nunca se usa fuera de los tests.
 */
public final class TokensDePrueba {

	/** Base64 de "llave-de-prueba-solo-para-tests-no-usar-en-produccion" (53 bytes). */
	public static final String SECRETO = "bGxhdmUtZGUtcHJ1ZWJhLXNvbG8tcGFyYS10ZXN0cy1uby11c2FyLWVuLXByb2R1Y2Npb24=";
	/** Otra llave válida, para simular un token firmado por un tercero. */
	public static final String OTRO_SECRETO = "b3RyYS1sbGF2ZS1kZS1wcnVlYmEtcXVlLWxhLWFwaS1uby1jb25vY2UtMTIzNA==";
	public static final String EMISOR = "afiliados-api";

	private TokensDePrueba() {
	}

	/** Token válido (emisor correcto, vence en 5 minutos) con los roles indicados. */
	public static String conRoles(String... roles) {
		return firmar(SECRETO, claims -> claims.claim("roles", List.of(roles)));
	}

	/** Firma con la llave indicada; el ajuste permite cambiar o quitar claims para probar rechazos. */
	public static String firmar(String secretoBase64, Consumer<JwtClaimsSet.Builder> ajuste) {
		Instant ahora = Instant.now();
		JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
				.issuer(EMISOR)
				.subject("usuario-prueba")
				.issuedAt(ahora)
				.expiresAt(ahora.plus(Duration.ofMinutes(5)));
		ajuste.accept(claims);
		NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(JwtConfig.llaveHmac(secretoBase64)));
		return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims.build()))
				.getTokenValue();
	}
}
