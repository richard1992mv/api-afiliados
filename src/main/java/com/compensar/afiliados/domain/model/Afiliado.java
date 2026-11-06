package com.compensar.afiliados.domain.model;

import com.compensar.afiliados.domain.exception.AfiliadoInactivoException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad raíz del dominio. No expone setters: su estado solo cambia mediante
 * métodos de negocio que hacen cumplir las reglas RN2 y RN4.
 */
public class Afiliado {

	private static final int LONGITUD_MAXIMA_NOMBRE = 100;

	private final Long id;
	private final Documento documento;
	private String nombres;
	private String apellidos;
	private Email email;
	private BigDecimal salario;
	private Categoria categoria;
	private EstadoAfiliado estado;

	private Afiliado(Long id, Documento documento, String nombres, String apellidos, Email email,
			BigDecimal salario, Categoria categoria, EstadoAfiliado estado) {
		this.id = id;
		this.documento = requerido(documento, "El documento es obligatorio");
		this.nombres = validarNombre(nombres, "nombres");
		this.apellidos = validarNombre(apellidos, "apellidos");
		this.email = requerido(email, "El email es obligatorio");
		this.salario = requerido(salario, "El salario es obligatorio");
		this.categoria = requerido(categoria, "La categoría es obligatoria");
		this.estado = requerido(estado, "El estado es obligatorio");
	}

	/**
	 * Crea un afiliado nuevo: queda ACTIVO y con la categoría calculada (RN2).
	 */
	public static Afiliado registrar(Documento documento, String nombres, String apellidos, Email email,
			BigDecimal salario, BigDecimal smmlv) {
		Categoria categoria = Categoria.calcular(salario, smmlv);
		return new Afiliado(null, documento, nombres, apellidos, email, salario, categoria, EstadoAfiliado.ACTIVO);
	}

	/**
	 * Reconstruye un afiliado existente (por ejemplo, leído desde persistencia).
	 */
	public static Afiliado reconstituir(Long id, Documento documento, String nombres, String apellidos,
			Email email, BigDecimal salario, Categoria categoria, EstadoAfiliado estado) {
		return new Afiliado(requerido(id, "El id es obligatorio"), documento, nombres, apellidos, email,
				salario, categoria, estado);
	}

	/**
	 * Actualiza los datos del afiliado y recalcula su categoría (RN2).
	 * RN4: un afiliado inactivo no se puede actualizar.
	 * Se valida todo antes de asignar, para no dejar el objeto a medio modificar.
	 */
	public void actualizar(String nombres, String apellidos, Email email, BigDecimal salario, BigDecimal smmlv) {
		validarActivo();
		String nuevosNombres = validarNombre(nombres, "nombres");
		String nuevosApellidos = validarNombre(apellidos, "apellidos");
		Email nuevoEmail = requerido(email, "El email es obligatorio");
		Categoria nuevaCategoria = Categoria.calcular(salario, smmlv);

		this.nombres = nuevosNombres;
		this.apellidos = nuevosApellidos;
		this.email = nuevoEmail;
		this.salario = salario;
		this.categoria = nuevaCategoria;
	}

	/**
	 * Inactiva al afiliado. RN4: si ya está inactivo, no se puede modificar.
	 */
	public void inactivar() {
		validarActivo();
		this.estado = EstadoAfiliado.INACTIVO;
	}

	public boolean estaActivo() {
		return estado == EstadoAfiliado.ACTIVO;
	}

	private void validarActivo() {
		if (!estaActivo()) {
			throw new AfiliadoInactivoException(id);
		}
	}

	private static String validarNombre(String valor, String campo) {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
		}
		String limpio = valor.trim();
		if (limpio.length() > LONGITUD_MAXIMA_NOMBRE) {
			throw new IllegalArgumentException(
					"El campo " + campo + " no puede superar " + LONGITUD_MAXIMA_NOMBRE + " caracteres");
		}
		return limpio;
	}

	private static <T> T requerido(T valor, String mensaje) {
		if (valor == null) {
			throw new IllegalArgumentException(mensaje);
		}
		return valor;
	}

	public Long getId() {
		return id;
	}

	public Documento getDocumento() {
		return documento;
	}

	public String getNombres() {
		return nombres;
	}

	public String getApellidos() {
		return apellidos;
	}

	public Email getEmail() {
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

	/**
	 * Igualdad de entidad: dos afiliados son el mismo si tienen el mismo id.
	 */
	@Override
	public boolean equals(Object otro) {
		if (this == otro) {
			return true;
		}
		if (!(otro instanceof Afiliado afiliado) || id == null) {
			return false;
		}
		return id.equals(afiliado.id);
	}

	@Override
	public int hashCode() {
		return id == null ? System.identityHashCode(this) : Objects.hash(id);
	}
}
