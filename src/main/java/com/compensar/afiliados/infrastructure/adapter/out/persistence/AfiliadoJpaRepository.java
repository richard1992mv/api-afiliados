package com.compensar.afiliados.infrastructure.adapter.out.persistence;

import com.compensar.afiliados.domain.model.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data. Es un detalle de infraestructura: el núcleo usa {@code AfiliadoRepositoryPort}.
 * Las consultas derivadas usan parámetros enlazados, lo que evita inyección SQL.
 */
interface AfiliadoJpaRepository extends JpaRepository<AfiliadoEntity, Long> {

	boolean existsByTipoDocumentoAndNumeroDocumento(TipoDocumento tipoDocumento, String numeroDocumento);
}
