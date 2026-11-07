# 0001. Arquitectura hexagonal

- **Estado:** Aceptada
- **Fecha:** 2026-11-07

## Contexto

La API gestiona afiliados de una caja de compensación. Su valor está en reglas de negocio que no dependen
de la tecnología:

- **RN1:** el documento (tipo y número) es único.
- **RN2:** la categoría se calcula según el salario en SMMLV.
- **RN3:** solo se permiten los tipos de documento CC, CE, TI y PA.
- **RN4:** un afiliado inactivo no se puede modificar (ni actualizar ni volver a inactivar).

Esas reglas deben poder probarse sin levantar Spring ni una base de datos, y sobrevivir a cambios de
framework. Spring Boot cambia de versión mayor con frecuencia (este proyecto ya usa la 4) y la persistencia
podría cambiar (JPA hoy, otra tecnología mañana).

Alternativas consideradas:

- **Capas tradicionales** (controller → service → repository): más simple, pero el dominio suele terminar
  acoplado a JPA (entidades anotadas) y a Spring.
- **Arquitectura hexagonal** (puertos y adaptadores): el dominio no conoce la infraestructura; la
  infraestructura se conecta a través de interfaces.

## Decisión

Usamos arquitectura hexagonal con tres capas bajo `com.compensar.afiliados`:

| Capa | Paquete | Contiene | Puede depender de |
|---|---|---|---|
| Dominio | `domain` | Entidades, value objects (`Documento`, `Email`), reglas y excepciones | Solo Java (`java..`) |
| Aplicación | `application` | Puertos de entrada (casos de uso), puertos de salida (`AfiliadoRepositoryPort`) y servicios | Dominio |
| Infraestructura | `infrastructure` | Controladores REST, DTO, entidades JPA, repositorios, mappers, seguridad y beans de Spring | Aplicación y dominio |

Regla de dependencias: `infrastructure → application → domain`, nunca al revés.

La regla no queda solo escrita: **`ArquitecturaHexagonalTest` (ArchUnit) la verifica en cada build**. El
dominio no puede usar Spring, JPA ni otras capas, la aplicación no puede usar infraestructura ni frameworks,
y se comprueba el orden de las capas. Un cambio que la viole hace fallar el pipeline.

## Consecuencias

**Positivas**

- El dominio es Java puro y se prueba con tests unitarios rápidos, sin Spring ni base de datos.
- Las reglas RN1 a RN4 viven en un solo lugar y no se duplican en controladores ni repositorios.
- Cambiar la persistencia o el framework web solo toca adaptadores en `infrastructure`.
- La regla se hace cumplir automáticamente: no depende de que alguien la recuerde en la revisión.

**Negativas**

- Más clases: entidad de dominio y entidad JPA separadas, comandos, DTO y mappers entre ellos.
- Como la aplicación no usa Spring, no puede usar `@Transactional`: hoy cada operación de "buscar y
  guardar" ocurre en transacciones separadas. Es un riesgo pendiente (ver el README); la solución prevista
  es un bloqueo optimista (`@Version`) o un decorador transaccional en infraestructura.
- Quien llega al proyecto necesita entender puertos y adaptadores antes de contribuir.
