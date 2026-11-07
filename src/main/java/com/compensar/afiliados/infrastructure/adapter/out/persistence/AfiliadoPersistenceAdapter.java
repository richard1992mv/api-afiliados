package com.compensar.afiliados.infrastructure.adapter.out.persistence;

import com.compensar.afiliados.application.port.out.AfiliadoRepositoryPort;
import com.compensar.afiliados.domain.exception.DocumentoDuplicadoException;
import com.compensar.afiliados.domain.model.Afiliado;
import com.compensar.afiliados.domain.model.Documento;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Adaptador de salida: implementa el puerto del núcleo usando Spring Data JPA.
 */
@Component
public class AfiliadoPersistenceAdapter implements AfiliadoRepositoryPort {

	private final AfiliadoJpaRepository repositorio;

	AfiliadoPersistenceAdapter(AfiliadoJpaRepository repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	public Afiliado guardar(Afiliado afiliado) {
		try {
			// saveAndFlush fuerza el INSERT/UPDATE aquí, para capturar la violación del índice único.
			AfiliadoEntity guardada = repositorio.saveAndFlush(AfiliadoPersistenceMapper.aEntidad(afiliado));
			return AfiliadoPersistenceMapper.aDominio(guardada);
		} catch (DataIntegrityViolationException e) {
			// RN1 ante concurrencia: dos registros simultáneos pasan la validación del servicio,
			// pero la BD rechaza el segundo. Se traduce a la excepción de dominio (409), y el
			// mensaje de MySQL, que incluye el número de documento, no se propaga ni se registra.
			if (esViolacionDocumentoUnico(e)) {
				throw new DocumentoDuplicadoException(afiliado.getDocumento());
			}
			throw e;
		}
	}

	@Override
	public Optional<Afiliado> buscarPorId(Long id) {
		return repositorio.findById(id).map(AfiliadoPersistenceMapper::aDominio);
	}

	@Override
	public List<Afiliado> listar() {
		return repositorio.findAll(Sort.by("id")).stream()
				.map(AfiliadoPersistenceMapper::aDominio)
				.toList();
	}

	@Override
	public boolean existePorDocumento(Documento documento) {
		return repositorio.existsByTipoDocumentoAndNumeroDocumento(documento.tipo(), documento.numero());
	}

	private static boolean esViolacionDocumentoUnico(DataIntegrityViolationException e) {
		String mensaje = e.getMostSpecificCause().getMessage();
		return mensaje != null && mensaje.toLowerCase(Locale.ROOT).contains(AfiliadoEntity.UK_DOCUMENTO);
	}
}
