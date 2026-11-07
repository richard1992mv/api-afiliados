package com.compensar.afiliados.infrastructure.adapter.out.persistence;

import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * PRUEBA DE DETECCIÓN (NO FUSIONAR): consulta vulnerable a inyección SQL a propósito.
 * Con numero = "' OR '1'='1" devolvería todos los afiliados.
 */
class BusquedaInsegura {

	private final EntityManager entityManager;

	BusquedaInsegura(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	List<?> porDocumento(String numero) {
		return entityManager
				.createNativeQuery("SELECT * FROM afiliados WHERE numero_documento = '" + numero + "'")
				.getResultList();
	}
}
