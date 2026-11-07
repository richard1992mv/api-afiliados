## Qué cambia y por qué

<!-- Una o dos frases. Si corrige un hallazgo, enlaza el check, la alerta o el issue. -->

## Tipo de cambio

- [ ] `feat` nueva funcionalidad
- [ ] `fix` corrección
- [ ] `sec` seguridad
- [ ] `refactor` / `test` / `docs` / `build` / `ci` / `chore`

## Checklist

**Lo verifica el pipeline** (el PR no se puede fusionar si falla):

- [ ] Las pruebas pasan en local (`./mvnw verify`)
- [ ] La cobertura de líneas sigue en 80 % o más
- [ ] No hay hallazgos críticos ni altos (Semgrep, SonarCloud, Dependency-Check, trivy, ZAP)
- [ ] No hay secretos: credenciales, tokens o `.env` (gitleaks)
- [ ] Los commits siguen Conventional Commits en español (`tipo(alcance): descripción`, commitlint)

**Lo verifica quien revisa:**

- [ ] Si cambia la arquitectura, el pipeline o una herramienta de seguridad, hay un ADR nuevo en [`docs/adr/`](../docs/adr/README.md)
- [ ] Si se acepta un riesgo o se suprime un hallazgo, la justificación está en el código o en la tabla de riesgos del `README.md`
- [ ] No se agregan datos personales reales (documentos, correos) en pruebas, logs ni ejemplos (Ley 1581)

## Cómo se probó

<!-- Comandos, colección Postman o capturas. Para cambios de seguridad, incluye el control negativo
     (la prueba que falla sin el cambio). -->
