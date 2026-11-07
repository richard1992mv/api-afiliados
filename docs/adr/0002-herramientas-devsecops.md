# 0002. Herramientas DevSecOps del pipeline

- **Estado:** Aceptada
- **Fecha:** 2026-11-07

## Contexto

La API maneja datos personales (Ley 1581) y debe tener seguridad verificable en cada cambio, no revisiones
manuales esporádicas. Las restricciones son:

- **Solo herramientas open source o gratuitas.**
- Repositorio público en GitHub, con GitHub Actions como CI.
- Ninguna técnica cubre todo. Lo vimos en la prueba de la Fase 6: trivy no detectó una API key genérica que
  gitleaks y SonarCloud sí detectaron; Dependency-Check no ve código propio, y SAST no ve dependencias.
- El pipeline es superficie de ataque: en marzo de 2026 se comprometieron los tags de `trivy-action`
  (CVE-2026-33634).

## Decisión

Usamos varias herramientas en capas. Cada una cubre una técnica y todas hacen fallar el pipeline:

| Técnica | Herramienta | Alternativas consideradas | Por qué esta |
|---|---|---|---|
| Pruebas y cobertura | JUnit, Testcontainers, JaCoCo (≥ 80 %), ArchUnit | H2 en memoria | Testcontainers prueba contra MySQL real, la misma versión que producción |
| Secret scanning | gitleaks (historial completo) y trivy (fs) | Solo push protection de GitHub | gitleaks detecta secretos genéricos y revisa todo el historial, no solo el último commit |
| SAST | Semgrep (`p/java`, `p/owasp-top-ten`) y SonarCloud | CodeQL | Semgrep es rápido y explica cada regla; Sonar suma calidad y un quality gate sobre el código nuevo |
| SCA | OWASP Dependency-Check (CVSS ≥ 7) y Dependabot | Snyk (plan gratuito limitado) | Open source y usa el NVD directamente; Dependabot propone las actualizaciones |
| Contenedor | hadolint y trivy (image) | Grype | trivy cubre sistema operativo y JAR en un solo escaneo, y publica SARIF |
| DAST | OWASP ZAP (API scan con token ADMIN) | Burp Suite (de pago) | Estándar de OWASP, entiende OpenAPI y se configura por reglas (`.zap/rules.tsv`) |
| Inventario | CycloneDX (SBOM) | SPDX | Formato de OWASP, nativo en Maven y con información de seguridad |
| Convención de commits | commitlint (npm con lockfile) | Imagen Docker de commitlint | La imagen no publica tags por versión; npm con `package-lock.json` fija cada paquete por hash sha512 |

Reglas transversales del pipeline:

- Acciones fijadas por **SHA de commit** e imágenes por **digest**, actualizadas por Dependabot con 7 días de
  espera (cooldown).
- `permissions: contents: read` por defecto; cada job pide solo lo que necesita.
- Secretos únicamente en GitHub Secrets (`SONAR_TOKEN`, `NVD_API_KEY`); el DAST genera credenciales
  aleatorias en cada ejecución.
- El ruleset de `main` y `develop` exige los 9 checks: nadie puede fusionar con uno en rojo.

## Consecuencias

**Positivas**

- Cada técnica tiene al menos una herramienta, y las críticas tienen dos (secretos y SAST).
- Los hallazgos llegan antes de fusionar, con reportes descargables (SARIF, HTML, SBOM).
- Todo es reproducible en local con Docker y las mismas versiones.

**Negativas**

- **Mantenimiento:** versiones, SHA, falsos positivos y cambios de las herramientas. En este proyecto ya
  ocurrieron: gitleaks-action v2 dejó de funcionar al retirarse Node 20, y SonarCloud gratuito no analiza
  ramas distintas de `main`.
- **Falsos positivos** que hay que revisar y suprimir con justificación (`.gitleaksignore`,
  `@SuppressWarnings`, `dependency-check-suppressions.xml`, `.zap/rules.tsv`).
- **Duración:** el pipeline completo tarda unos 5 minutos; la primera descarga del NVD, más.
- **Dependencia de servicios externos gratuitos** (SonarCloud, la API del NVD) con límites que pueden cambiar.
