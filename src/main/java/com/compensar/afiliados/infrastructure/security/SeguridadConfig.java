package com.compensar.afiliados.infrastructure.security;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Seguridad HTTP: la API es un OAuth2 Resource Server que solo acepta JWT.
 *
 * <ul>
 *   <li>Públicos: health y la documentación OpenAPI/Swagger.</li>
 *   <li>GET /api/**: ADMIN o CONSULTA.</li>
 *   <li>Todo lo demás (POST, PUT, PATCH, DELETE y cualquier ruta no listada): solo ADMIN.
 *       Lo que no se permite explícitamente queda denegado por defecto.</li>
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SeguridadConfig {

	static final String CLAIM_ROLES = "roles";

	private static final String[] RUTAS_PUBLICAS = {
			"/actuator/health", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**"};

	// java:S4502 (CSRF deshabilitado) revisado: falso positivo para una API stateless con Bearer token,
	// ver la explicación en .csrf(...). Si algún día se usan cookies o sesión, hay que reactivar CSRF.
	@SuppressWarnings("java:S4502")
	@Bean
	SecurityFilterChain cadenaDeSeguridad(HttpSecurity http,
			@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) throws Exception {
		http
				// CSRF protege a las apps que autentican con cookies: el navegador las envía solo, y un sitio
				// malicioso puede disparar peticiones con la sesión de la víctima. Esta API no usa cookies ni
				// sesión: el token viaja en la cabecera Authorization, que el navegador nunca agrega por su
				// cuenta, así que un sitio ajeno no puede falsificar una petición autenticada.
				.csrf(AbstractHttpConfigurer::disable)
				// Stateless: no se crea HttpSession; cada petición se autentica solo con su token.
				.sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(reglas -> reglas
						// Los reenvíos internos de error no deben convertir un 500 en un 401.
						.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
						.requestMatchers(HttpMethod.GET, RUTAS_PUBLICAS).permitAll()
						.requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole(Rol.ADMIN.name(), Rol.CONSULTA.name())
						.anyRequest().hasRole(Rol.ADMIN.name()))
				.oauth2ResourceServer(servidor -> servidor
						.jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorDeAutenticacion()))
						.authenticationEntryPoint(puntoDeEntrada(resolver))
						.accessDeniedHandler(accesoDenegado(resolver)))
				.exceptionHandling(errores -> errores
						.authenticationEntryPoint(puntoDeEntrada(resolver))
						.accessDeniedHandler(accesoDenegado(resolver)))
				.headers(cabeceras -> cabeceras
						// HSTS: el navegador solo usará HTTPS con este dominio durante un año.
						// Spring la envía únicamente en respuestas HTTPS (en HTTP no tiene efecto).
						.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31_536_000))
						// nosniff: el navegador no "adivina" el tipo de contenido (evita ejecutar JSON como script).
						.contentTypeOptions(Customizer.withDefaults())
						// DENY: la respuesta no puede mostrarse dentro de un iframe (clickjacking).
						.frameOptions(marcos -> marcos.deny())
						.referrerPolicy(referer -> referer.policy(ReferrerPolicy.NO_REFERRER)));
		return http.build();
	}

	/** Lee el claim "roles" (p. ej. ["ADMIN"]) y lo convierte en autoridades ROLE_ADMIN. */
	public static JwtGrantedAuthoritiesConverter convertidorDeRoles() {
		JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
		roles.setAuthoritiesClaimName(CLAIM_ROLES);
		roles.setAuthorityPrefix("ROLE_");
		return roles;
	}

	static JwtAuthenticationConverter convertidorDeAutenticacion() {
		JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
		convertidor.setJwtGrantedAuthoritiesConverter(convertidorDeRoles());
		return convertidor;
	}

	/**
	 * 401: agrega la cabecera estándar WWW-Authenticate: Bearer (RFC 6750) y delega el cuerpo en
	 * GlobalExceptionHandler, para que todos los errores compartan el mismo formato ProblemDetail.
	 */
	static AuthenticationEntryPoint puntoDeEntrada(HandlerExceptionResolver resolver) {
		BearerTokenAuthenticationEntryPoint bearer = new BearerTokenAuthenticationEntryPoint();
		return (peticion, respuesta, excepcion) -> {
			bearer.commence(peticion, respuesta, excepcion);
			resolver.resolveException(peticion, respuesta, null, excepcion);
		};
	}

	/** 403: token válido pero sin el rol necesario. */
	static AccessDeniedHandler accesoDenegado(HandlerExceptionResolver resolver) {
		return (peticion, respuesta, excepcion) -> resolver.resolveException(peticion, respuesta, null, excepcion);
	}
}
