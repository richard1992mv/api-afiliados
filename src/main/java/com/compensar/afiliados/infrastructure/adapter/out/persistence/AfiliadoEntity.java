package com.compensar.afiliados.infrastructure.adapter.out.persistence;

import com.compensar.afiliados.domain.model.Categoria;
import com.compensar.afiliados.domain.model.EstadoAfiliado;
import com.compensar.afiliados.domain.model.TipoDocumento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

/**
 * Entidad JPA: representa la fila de la tabla. Solo vive en infraestructura;
 * nunca sale de este paquete (el núcleo trabaja con {@code Afiliado}).
 */
@Entity
@Table(name = "afiliados", uniqueConstraints = @UniqueConstraint(
		name = AfiliadoEntity.UK_DOCUMENTO, columnNames = {"tipo_documento", "numero_documento"}))
public class AfiliadoEntity {

	static final String UK_DOCUMENTO = "uk_afiliados_documento";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// VARCHAR explícito: en MySQL Hibernate mapea los enum a ENUM nativo y la validación fallaría.
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "tipo_documento", nullable = false, length = 2)
	private TipoDocumento tipoDocumento;

	@Column(name = "numero_documento", nullable = false, length = 20)
	private String numeroDocumento;

	@Column(nullable = false, length = 100)
	private String nombres;

	@Column(nullable = false, length = 100)
	private String apellidos;

	@Column(nullable = false, length = 254)
	private String email;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal salario;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, length = 1)
	private Categoria categoria;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, length = 10)
	private EstadoAfiliado estado;

	/** Requerido por JPA. */
	protected AfiliadoEntity() {
	}

	AfiliadoEntity(Long id, TipoDocumento tipoDocumento, String numeroDocumento, String nombres, String apellidos,
			String email, BigDecimal salario, Categoria categoria, EstadoAfiliado estado) {
		this.id = id;
		this.tipoDocumento = tipoDocumento;
		this.numeroDocumento = numeroDocumento;
		this.nombres = nombres;
		this.apellidos = apellidos;
		this.email = email;
		this.salario = salario;
		this.categoria = categoria;
		this.estado = estado;
	}

	public Long getId() {
		return id;
	}

	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public String getNombres() {
		return nombres;
	}

	public String getApellidos() {
		return apellidos;
	}

	public String getEmail() {
		return email;
	}

	public BigDecimal getSalario() {
		return salario;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public EstadoAfiliado getEstado() {
		return estado;
	}
}
