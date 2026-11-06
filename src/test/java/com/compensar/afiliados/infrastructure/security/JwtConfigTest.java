package com.compensar.afiliados.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Validación real de tokens (sin Spring): cada test es un ataque o un error de configuración concreto.
 */
class JwtConfigTest {

	private final JwtDecoder decoder =
			new JwtConfig().jwtDecoder(new JwtProperties(TokensDePrueba.SECRETO, TokensDePrueba.EMISOR));

	@Test
	void aceptaUnTokenValidoYConservaLosRoles() {
		Jwt jwt = decoder.decode(TokensDePrueba.conRoles("ADMIN"));

		assertThat(jwt.getSubject()).isEqualTo("usuario-prueba");
		assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ADMIN");
	}

	@Test
	void rechazaUnTokenFirmadoConOtraLlave() {
		String falsificado = TokensDePrueba.firmar(TokensDePrueba.OTRO_SECRETO, claims -> claims.claim("roles", List.of("ADMIN")));

		assertThatThrownBy(() -> decoder.decode(falsificado)).isInstanceOf(JwtException.class);
	}

	@Test
	void rechazaUnTokenAlteradoDespuesDeFirmado() {
		// Cambiar CONSULTA por ADMIN en el payload invalida la firma.
		String[] partes = TokensDePrueba.conRoles("CONSULTA").split("\\.");
		String payload = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8)
				.replace("CONSULTA", "ADMIN");
		String alterado = partes[0] + "." + Base64.getUrlEncoder().withoutPadding()
				.encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + partes[2];

		assertThatThrownBy(() -> decoder.decode(alterado)).isInstanceOf(JwtException.class);
	}

	@Test
	void rechazaUnTokenSinFirmaAlgNone() {
		// Ataque clásico: declarar "alg":"none" para saltarse la verificación de la firma.
		String cabecera = base64Url("{\"alg\":\"none\"}");
		String payload = base64Url("{\"iss\":\"afiliados-api\",\"sub\":\"x\",\"roles\":[\"ADMIN\"],\"exp\":"
				+ Instant.now().plusSeconds(300).getEpochSecond() + "}");

		assertThatThrownBy(() -> decoder.decode(cabecera + "." + payload + ".")).isInstanceOf(JwtException.class);
	}

	@Test
	void rechazaUnTokenVencido() {
		String vencido = TokensDePrueba.firmar(TokensDePrueba.SECRETO, claims -> claims
				.issuedAt(Instant.now().minusSeconds(900))
				.expiresAt(Instant.now().minusSeconds(300)));

		assertThatThrownBy(() -> decoder.decode(vencido)).isInstanceOf(JwtException.class);
	}

	@Test
	void rechazaUnTokenSinExpiracionPorqueNuncaVenceria() {
		String sinExp = TokensDePrueba.firmar(TokensDePrueba.SECRETO,
				claims -> claims.claims(mapa -> mapa.remove(JwtClaimNames.EXP)));

		assertThatThrownBy(() -> decoder.decode(sinExp)).isInstanceOf(JwtException.class);
	}

	@Test
	void rechazaUnTokenDeOtroEmisor() {
		String otroEmisor = TokensDePrueba.firmar(TokensDePrueba.SECRETO, claims -> claims.issuer("otro-sistema"));

		assertThatThrownBy(() -> decoder.decode(otroEmisor)).isInstanceOf(JwtException.class);
	}

	@Test
	void laLlaveDebeEstarEnBase64SinRevelarElValor() {
		assertThatThrownBy(() -> JwtConfig.llaveHmac("no es base64 !!"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Base64")
				.hasMessageNotContaining("no es base64");
	}

	@Test
	void laLlaveDebeTenerAlMenos256Bits() {
		String corta = Base64.getEncoder().encodeToString(new byte[31]);

		assertThatThrownBy(() -> JwtConfig.llaveHmac(corta))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("256 bits");
		assertThat(JwtConfig.llaveHmac(Base64.getEncoder().encodeToString(new byte[32])).getEncoded()).hasSize(32);
	}

	@Test
	void elConvertidorTransformaElClaimRolesEnAutoridades() {
		Jwt jwt = decoder.decode(TokensDePrueba.conRoles("ADMIN", "CONSULTA"));

		assertThat(SeguridadConfig.convertidorDeRoles().convert(jwt)).extracting(GrantedAuthority::getAuthority)
				.containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_CONSULTA");
	}

	@Test
	void lasPropiedadesNoImprimenElSecreto() {
		assertThat(new JwtProperties(TokensDePrueba.SECRETO, TokensDePrueba.EMISOR).toString())
				.doesNotContain(TokensDePrueba.SECRETO)
				.contains("****");
	}

	private static String base64Url(String texto) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
	}
}
