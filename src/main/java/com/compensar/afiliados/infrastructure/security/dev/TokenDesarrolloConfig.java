package com.compensar.afiliados.infrastructure.security.dev;

import com.compensar.afiliados.infrastructure.security.JwtConfig;
import com.compensar.afiliados.infrastructure.security.JwtProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SOLO perfil "dev": la API emite sus propios tokens de prueba.
 * En cualquier otro perfil estos beans no existen, y /dev/** cae en la regla general (solo ADMIN con token).
 * En producción los tokens los emite un proveedor de identidad, nunca la propia API.
 */
@Configuration(proxyBeanMethods = false)
@Profile("dev")
public class TokenDesarrolloConfig {

	@Bean
	JwtEncoder jwtEncoder(JwtProperties propiedades) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(JwtConfig.llaveHmac(propiedades.secreto())));
	}

	/** Cadena propia para /dev/** con prioridad sobre la general: el emisor de tokens no pide token. */
	// java:S4502 revisado: igual que en SeguridadConfig, stateless y sin cookies (y solo existe en el perfil dev).
	@SuppressWarnings("java:S4502")
	@Bean
	@Order(1)
	SecurityFilterChain cadenaDeDesarrollo(HttpSecurity http) throws Exception {
		http.securityMatcher("/dev/**")
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(reglas -> reglas
						.requestMatchers(HttpMethod.POST, TokenDesarrolloController.RUTA).permitAll()
						.anyRequest().denyAll());
		return http.build();
	}
}
