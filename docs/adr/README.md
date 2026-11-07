# Registro de decisiones de arquitectura (ADR)

Un ADR (*Architecture Decision Record*) documenta una decisión importante: qué se decidió, por qué y qué
consecuencias tiene. El código muestra *qué* se hizo; el ADR explica *por qué*, para que nadie la revierta
sin conocer el contexto ni la repita sin necesidad.

| # | Decisión | Estado |
|---|---|---|
| [0001](0001-arquitectura-hexagonal.md) | Arquitectura hexagonal | Aceptada |
| [0002](0002-herramientas-devsecops.md) | Herramientas DevSecOps del pipeline | Aceptada |

## Cuándo escribir uno

Cuando el cambio afecta la arquitectura (capas, puertos, persistencia), agrega o cambia una herramienta del
pipeline, cambia un umbral de seguridad o acepta un riesgo que no es trivial. La plantilla de PR lo pregunta.

## Cómo escribirlo

1. Copia la plantilla de abajo en `NNNN-titulo-en-minusculas.md`, con el siguiente número libre.
2. Agrégalo a la tabla de este índice.
3. Súbelo en el mismo PR que el cambio que describe.

Un ADR aceptado no se edita para cambiar la decisión: se escribe uno nuevo y el anterior pasa a
*Reemplazada por NNNN*.

```markdown
# NNNN. Título

- **Estado:** Propuesta | Aceptada | Reemplazada por NNNN
- **Fecha:** AAAA-MM-DD

## Contexto
Qué problema o fuerza obliga a decidir. Hechos, restricciones y alternativas consideradas.

## Decisión
Qué se decidió, en forma afirmativa ("Usamos...").

## Consecuencias
Qué gana y qué pierde el proyecto. Incluye los costos y riesgos, no solo los beneficios.
```
