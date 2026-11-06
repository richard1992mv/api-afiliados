package com.compensar.afiliados.infrastructure.security;

/**
 * Roles que llegan en el claim "roles" del JWT.
 * ADMIN: todas las operaciones. CONSULTA: solo lectura (GET).
 */
public enum Rol {
	ADMIN,
	CONSULTA
}
