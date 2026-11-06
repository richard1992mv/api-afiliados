package com.compensar.afiliados;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// TODO Fase 3: reactivar este test configurando Testcontainers con MySQL 8
// (hoy falla porque no hay DataSource configurado para JPA/Flyway).
@SpringBootTest
@Disabled("Se reactiva en la Fase 3 con Testcontainers")
class AfiliadosApplicationTests {

	@Test
	void contextLoads() {
	}

}
