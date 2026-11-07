package com.compensar.afiliados;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Reglas de la arquitectura hexagonal (CLAUDE.md) como pruebas ejecutables.
 * Si alguien rompe la regla de dependencias, el build falla y el PR no se puede fusionar.
 */
@AnalyzeClasses(packages = "com.compensar.afiliados", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaHexagonalTest {

	private static final String DOMINIO = "com.compensar.afiliados.domain..";
	private static final String APLICACION = "com.compensar.afiliados.application..";
	private static final String INFRAESTRUCTURA = "com.compensar.afiliados.infrastructure..";

	private static final String[] FRAMEWORKS = {
			"org.springframework..", "jakarta.persistence..", "org.hibernate..", "lombok.."};

	@ArchTest
	static final ArchRule dominioNoDependeDeFrameworks = noClasses()
			.that().resideInAPackage(DOMINIO)
			.should().dependOnClassesThat().resideInAnyPackage(FRAMEWORKS)
			.because("el dominio es Java puro: sin Spring, JPA, Hibernate ni Lombok");

	@ArchTest
	static final ArchRule dominioNoDependeDeInfraestructuraNiAplicacion = noClasses()
			.that().resideInAPackage(DOMINIO)
			.should().dependOnClassesThat().resideInAnyPackage(INFRAESTRUCTURA, APLICACION)
			.because("las dependencias apuntan hacia adentro: infrastructure -> application -> domain");

	@ArchTest
	static final ArchRule dominioSoloUsaJavaYSiMismo = classes()
			.that().resideInAPackage(DOMINIO)
			.should().onlyDependOnClassesThat().resideInAnyPackage(DOMINIO, "java..")
			.because("cualquier librería nueva en el dominio debe ser una decisión explícita");

	@ArchTest
	static final ArchRule aplicacionNoDependeDeInfraestructura = noClasses()
			.that().resideInAPackage(APLICACION)
			.should().dependOnClassesThat().resideInAPackage(INFRAESTRUCTURA)
			.because("la aplicación define puertos; la infraestructura los implementa");

	@ArchTest
	static final ArchRule aplicacionNoDependeDeFrameworks = noClasses()
			.that().resideInAPackage(APLICACION)
			.should().dependOnClassesThat().resideInAnyPackage(FRAMEWORKS)
			.because("los servicios se registran como beans desde infrastructure/config, sin @Service");

	@ArchTest
	static final ArchRule capasHexagonales = layeredArchitecture().consideringOnlyDependenciesInLayers()
			.layer("Dominio").definedBy(DOMINIO)
			.layer("Aplicacion").definedBy(APLICACION)
			.layer("Infraestructura").definedBy(INFRAESTRUCTURA)
			.whereLayer("Infraestructura").mayNotBeAccessedByAnyLayer()
			.whereLayer("Aplicacion").mayOnlyBeAccessedByLayers("Infraestructura")
			.whereLayer("Dominio").mayOnlyBeAccessedByLayers("Aplicacion", "Infraestructura");
}
