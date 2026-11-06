# CLAUDE.md

## Proyecto

API REST **"afiliados"** para una caja de compensación. Es un ejercicio de prueba técnica.

## Stack

- Java 17
- Spring Boot 4 (4.1.2, definido en `pom.xml`)
- Maven (usar el wrapper: `./mvnw` / `mvnw.cmd`)
- MySQL 8
- Docker
- GitHub Actions (CI/CD)

## Arquitectura hexagonal (estricta)

Paquete base: `com.compensar.afiliados`

| Capa | Paquete | Contenido | Restricciones |
|------|---------|-----------|---------------|
| Dominio | `...domain` | Entidades, value objects, reglas de negocio, excepciones de dominio | **Sin Spring ni JPA**: Java puro |
| Aplicación | `...application` | Puertos de entrada (`port.in`, casos de uso), puertos de salida (`port.out`) y servicios que los implementan | Depende solo del dominio |
| Infraestructura | `...infrastructure` | Adaptadores web (controladores REST, DTO), persistencia (entidades JPA, repositorios, mappers) y configuración (beans de Spring) | Implementa los puertos; aquí vive todo el framework |

Regla de dependencias: `infrastructure → application → domain`, nunca al revés.

## Reglas

- **Idioma**: el código y los nombres de negocio van en español (clases, métodos, variables, endpoints).
- **Sin secretos en el código**: credenciales, URLs de BD y tokens se pasan por variables de entorno (p. ej. `${DB_PASSWORD}` en `application.properties`). Nunca se commitean `.env` ni credenciales.
- **Cobertura mínima del 80 %**, verificada con JaCoCo en el build.
- **Solo herramientas open source o gratuitas.**

## Forma de trabajar

El autor está aprendiendo DevSecOps:

1. **Antes de cada cambio**, explicar qué se va a hacer y por qué.
2. **Al final de cada tarea**, resumir qué se aprendió (conceptos, buenas prácticas, decisiones de seguridad).
