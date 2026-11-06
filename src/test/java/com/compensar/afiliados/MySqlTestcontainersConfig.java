package com.compensar.afiliados;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

/**
 * MySQL real en un contenedor desechable para las pruebas de integración.
 * Con @ServiceConnection, Spring toma la URL y las credenciales que genera el contenedor:
 * los tests no necesitan secretos ni variables de entorno, ni en local ni en CI.
 * Usa la misma imagen que docker-compose.yml para probar contra la versión de desarrollo.
 */
@TestConfiguration(proxyBeanMethods = false)
public class MySqlTestcontainersConfig {

	public static final String IMAGEN_MYSQL = "mysql:8.4";

	@Bean
	@ServiceConnection
	MySQLContainer mysqlContainer() {
		return new MySQLContainer(IMAGEN_MYSQL);
	}
}
