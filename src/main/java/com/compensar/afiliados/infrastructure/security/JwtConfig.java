package com.compensar.afiliados.infrastructure.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

/**
 * Validación de los JWT entrantes (HS256 con llave simétrica).
 * Un token se acepta solo si: la firma HS256 es válida, "iss" coincide, "exp" existe y no ha vencido
 * (y "nbf", si viene, ya se cumplió).
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

	/** HS256 requiere una llave de al menos 256 bits (RFC 7518, sección 3.2). */
	static final int BYTES_MINIMOS_LLAVE = 32;

	@Bean
	JwtDecoder jwtDecoder(JwtProperties propiedades) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(llaveHmac(propiedades.secreto()))
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		decoder.setJwtValidator(validador(propiedades.emisor()));
		return decoder;
	}

	/**
	 * Por defecto Spring acepta tokens sin "exp", que nunca vencerían: aquí "exp" es obligatorio.
	 */
	static OAuth2TokenValidator<Jwt> validador(String emisor) {
		return new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefaultWithIssuer(emisor),
				new JwtClaimValidator<Instant>(JwtClaimNames.EXP, Objects::nonNull));
	}

	/**
	 * Decodifica la llave Base64 y exige 256 bits. Exigir Base64 empuja a generarla con
	 * {@code openssl rand -base64 32} en lugar de usar una frase memorable (con poca entropía).
	 * Los mensajes de error nunca incluyen el valor recibido.
	 */
	public static SecretKey llaveHmac(String secretoBase64) {
		byte[] bytes;
		try {
			bytes = Base64.getDecoder().decode(secretoBase64.strip());
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("JWT_SECRET debe estar en Base64 (genérelo con: openssl rand -base64 32)");
		}
		if (bytes.length < BYTES_MINIMOS_LLAVE) {
			throw new IllegalStateException("JWT_SECRET debe tener al menos 256 bits (" + BYTES_MINIMOS_LLAVE
					+ " bytes decodificados); tiene " + bytes.length + " bytes");
		}
		return new SecretKeySpec(bytes, "HmacSHA256");
	}
}
