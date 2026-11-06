package com.compensar.afiliados.infrastructure.adapter.out.persistence;

import com.compensar.afiliados.domain.model.Afiliado;
import com.compensar.afiliados.domain.model.Categoria;
import com.compensar.afiliados.domain.model.Documento;
import com.compensar.afiliados.domain.model.Email;
import com.compensar.afiliados.domain.model.EstadoAfiliado;
import com.compensar.afiliados.domain.model.TipoDocumento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AfiliadoPersistenceMapperTest {

	@Test
	void convierteDeDominioAEntidadYDeVuelta() {
		Afiliado original = Afiliado.reconstituir(7L, new Documento(TipoDocumento.PA, "AB123456"), "Ana",
				"Pérez", new Email("ana@correo.com"), new BigDecimal("3500000.50"), Categoria.B,
				EstadoAfiliado.INACTIVO);

		AfiliadoEntity entidad = AfiliadoPersistenceMapper.aEntidad(original);
		Afiliado reconstruido = AfiliadoPersistenceMapper.aDominio(entidad);

		assertThat(entidad.getTipoDocumento()).isEqualTo(TipoDocumento.PA);
		assertThat(entidad.getNumeroDocumento()).isEqualTo("AB123456");
		assertThat(entidad.getEmail()).isEqualTo("ana@correo.com");
		assertThat(reconstruido).usingRecursiveComparison().isEqualTo(original);
	}

	@Test
	void unAfiliadoNuevoGeneraUnaEntidadSinId() {
		Afiliado nuevo = Afiliado.registrar(new Documento(TipoDocumento.CC, "123456"), "Ana", "Pérez",
				new Email("ana@correo.com"), new BigDecimal("1000000"), new BigDecimal("1423500"));

		assertThat(AfiliadoPersistenceMapper.aEntidad(nuevo).getId()).isNull();
	}
}
