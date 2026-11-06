package com.compensar.afiliados.infrastructure.adapter.out.persistence;

import com.compensar.afiliados.domain.model.Afiliado;
import com.compensar.afiliados.domain.model.Documento;
import com.compensar.afiliados.domain.model.Email;

/**
 * Traduce entre la entidad JPA y el modelo de dominio.
 */
final class AfiliadoPersistenceMapper {

	private AfiliadoPersistenceMapper() {
	}

	static AfiliadoEntity aEntidad(Afiliado afiliado) {
		return new AfiliadoEntity(
				afiliado.getId(),
				afiliado.getDocumento().tipo(),
				afiliado.getDocumento().numero(),
				afiliado.getNombres(),
				afiliado.getApellidos(),
				afiliado.getEmail().valor(),
				afiliado.getSalario(),
				afiliado.getCategoria(),
				afiliado.getEstado());
	}

	/** Al reconstruir se vuelven a crear los value objects, que validan los datos leídos. */
	static Afiliado aDominio(AfiliadoEntity entidad) {
		return Afiliado.reconstituir(
				entidad.getId(),
				new Documento(entidad.getTipoDocumento(), entidad.getNumeroDocumento()),
				entidad.getNombres(),
				entidad.getApellidos(),
				new Email(entidad.getEmail()),
				entidad.getSalario(),
				entidad.getCategoria(),
				entidad.getEstado());
	}
}
