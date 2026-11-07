# API de afiliados

API REST para gestionar los afiliados de una caja de compensación (prueba técnica).
Java 17, Spring Boot 4, MySQL 8.4 y arquitectura hexagonal (`domain` → `application` → `infrastructure`).

## Ejecutar en local

```bash
cp .env.example .env        # completar los valores (JWT_SECRET: openssl rand -base64 32)
docker compose up -d --build
```

- API: `http://localhost:8080/api/v1/afiliados` (requiere token Bearer)
- Documentación: `http://localhost:8080/swagger-ui.html`
- Tokens de prueba: con `SPRING_PROFILES_ACTIVE=dev`, `POST /dev/token?roles=ADMIN` (solo en ese perfil)
- Pruebas de caja negra: [`postman/README.md`](postman/README.md)

## Pipeline de seguridad (`.github/workflows/ci.yml`)

| Job | Técnica | Herramienta | Falla el build si… |
|---|---|---|---|
| Compilar y probar | Pruebas + cobertura + SBOM | JUnit, Testcontainers, JaCoCo, CycloneDX | una prueba falla o la cobertura baja del 80 % |
| Secretos (gitleaks) | Secret scanning | gitleaks (historial completo) | encuentra un secreto |
| Análisis estático | Secret scanning | trivy (fs) | encuentra un secreto de proveedor conocido |
| SAST (Semgrep) | SAST | Semgrep `p/java` + `p/owasp-top-ten` | encuentra un hallazgo |
| Calidad (SonarCloud) | SAST + calidad | SonarCloud | falla el quality gate |
| SCA (OWASP Dependency-Check) | SCA | Dependency-Check | una dependencia tiene CVSS ≥ 7 |
| Seguridad del contenedor | Imagen | hadolint, trivy (image) | el Dockerfile tiene errores o hay CVE CRITICAL/HIGH con parche |
| DAST (OWASP ZAP) | DAST | ZAP API scan con token ADMIN | una regla `FAIL` de [`.zap/rules.tsv`](.zap/rules.tsv) |

Los hallazgos de Semgrep y trivy (imagen) se publican en la pestaña **Security** del repositorio.
El SBOM (`bom.json`, CycloneDX) se guarda 90 días como artifact de cada ejecución.

## Riesgos aceptados

Cada riesgo se revisó y se acepta en este contexto. La columna *Revisar si* indica cuándo deja de ser aceptable.

| Riesgo | Detectado por | Por qué se acepta | Revisar si |
|---|---|---|---|
| CSRF deshabilitado | Sonar `java:S4502`, ZAP `10202` (IGNORE) | La API es stateless: el token viaja en la cabecera `Authorization`, que el navegador nunca agrega solo. Sin cookies ni sesión no hay nada que un sitio ajeno pueda reutilizar. | se usan cookies o sesión |
| CSP limitada a `frame-ancestors 'none'` | Revisión de cabeceras (OWASP REST Security Cheat Sheet) | La API solo devuelve JSON con `nosniff`, así que no hay HTML donde inyectar scripts. Una CSP completa (`default-src 'none'`) rompería Swagger UI. | la API sirve HTML propio |
| Sin HTTPS en el entorno DAST | ZAP `10106` (WARN) | El entorno del pipeline es efímero y local al runner. En producción TLS termina en el proxy o ingress, y la API ya envía HSTS en respuestas HTTPS. | la API se expone directamente sin proxy TLS |
| Swagger UI responde HTML | ZAP `100001` (WARN) | Es la interfaz de documentación, HTML por diseño. El resto de la API responde `application/json` o `application/problem+json`. | — |
| OpenAPI y Swagger UI públicos | Revisión manual | Facilitan la evaluación de la prueba técnica. En producción se apagan con `SWAGGER_ENABLED=false`. | se despliega en producción |
| Cobertura parcial del DAST en altas | Análisis del escaneo | ZAP reutiliza el documento de ejemplo del OpenAPI y la regla RN1 (duplicado) corta muchos ataques al `POST`. Esos campos los cubren las pruebas de validación y el SAST. | — |
| `swagger-ui` fijado en el pom | SCA (CVE de DOMPurify) | springdoc 3.2.0 trae una versión vulnerable. La versión se fija en el pom y en `springdoc.swagger-ui.version`. | springdoc trae ≥ 5.34.1 (quitar el override) |
| Sonar solo en PRs y en `main` | Limitación del plan gratuito | Todo cambio llega a `develop` por un PR que ya pasó el quality gate. | se cambia de plan |

### Riesgos conocidos pendientes (no aceptados)

| Riesgo | Mitigación prevista |
|---|---|
| JWT HS256: quien valida también puede firmar | Migrar a un proveedor de identidad con RS256/JWKS (por ejemplo Keycloak) |
| El rol CONSULTA ve documento y email completos (Ley 1581) | Enmascarar datos personales según el rol |
| Listado sin paginación ni rate limiting (OWASP API4) | Paginación y límite de peticiones |
| Actualizaciones concurrentes pueden pisarse | Bloqueo optimista con `@Version` |
