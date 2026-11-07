# Estándar de desarrollo

Reglas obligatorias para todo cambio en este repositorio, sea del equipo interno o de un proveedor externo.
La mayoría **no dependen de la revisión humana**: el pipeline las verifica y el ruleset impide fusionar si
alguna falla.

## Reglas que hace cumplir el pipeline

| Regla | Umbral | Lo verifica | Configurado en |
|---|---|---|---|
| Todas las pruebas pasan | 0 fallos | Compilar y probar (JUnit, Testcontainers) | `pom.xml` |
| Cobertura de líneas | **≥ 80 %** | JaCoCo (`jacoco:check`) | `pom.xml` → `jacoco.cobertura.minima` |
| Arquitectura hexagonal | 0 violaciones | ArchUnit | `ArquitecturaHexagonalTest` |
| Sin secretos en el código ni en el historial | 0 hallazgos | gitleaks y trivy (fs) | `.gitleaks.toml`, `.gitleaksignore` |
| Código sin vulnerabilidades | 0 hallazgos | Semgrep (`p/java`, `p/owasp-top-ten`) | `ci.yml` |
| Quality gate | **Aprobado** (código nuevo: cobertura ≥ 80 %, calificación A en seguridad, confiabilidad y mantenibilidad, duplicación ≤ 3 %, hotspots 100 % revisados) | SonarCloud | Quality gate *Sonar way* |
| Dependencias sin vulnerabilidades altas | **0 con CVSS ≥ 7** | OWASP Dependency-Check | `pom.xml` → `failBuildOnCVSS` |
| Imagen sin vulnerabilidades corregibles | **0 CRITICAL/HIGH con parche** | trivy (image) | `ci.yml` |
| Dockerfile según buenas prácticas | 0 errores | hadolint | `ci.yml` |
| API sin fallos explotables en ejecución | **0 reglas FAIL** | OWASP ZAP | `.zap/rules.tsv` |
| Conventional Commits en español, asunto de máximo 72 caracteres | Todos los commits del PR | commitlint | `commitlint.config.mjs` |
| Cambios solo por PR, con los 9 checks en verde | Sin excepciones | Ruleset de `main` y `develop` | Settings → Rules |

## Reglas que verifica la revisión

| Regla | Cómo |
|---|---|
| ADR para cambios de arquitectura o de herramientas del pipeline | [`docs/adr/`](adr/README.md) |
| Sin datos personales reales en pruebas, logs ni ejemplos (Ley 1581) | Plantilla de PR |
| Configuración por variables de entorno; nunca credenciales en el código ni `.env` commiteado | `.env.example` como plantilla; gitleaks como red de seguridad |
| Solo herramientas open source o gratuitas | ADR 0002 |
| Acciones fijadas por SHA, imágenes por versión exacta y digest | Revisión del PR; Dependabot las mantiene |

## Excepciones

No se baja un umbral para que un PR pase. Si un hallazgo es un falso positivo o un riesgo aceptable:

1. **Se analiza** y se explica por qué no aplica en este contexto.
2. **Se suprime con el alcance mínimo:** la huella exacta (`.gitleaksignore`), la regla en un solo método
   (`@SuppressWarnings("java:Sxxxx")`), un CVE de una dependencia con fecha de vencimiento
   (`dependency-check-suppressions.xml`) o una regla de ZAP (`.zap/rules.tsv`). Nunca una carpeta entera ni
   la herramienta completa.
3. **Se documenta** en el código y, si es un riesgo de diseño, en la tabla de riesgos aceptados del README,
   con la condición en la que debe revisarse.
4. **Pasa por PR:** la excepción queda en el historial, revisada como cualquier otro cambio.
