# Política de seguridad

## Versiones con soporte

| Versión | Soporte |
|---|---|
| Rama `main` (última versión liberada) | ✅ Recibe correcciones de seguridad |
| Cualquier otra rama o versión anterior | ❌ |

## Cómo reportar una vulnerabilidad

**No abras un issue público.** Un issue es visible para cualquiera y expondría el fallo antes de corregirlo.

Repórtala de forma privada con
[GitHub Private Vulnerability Reporting](https://github.com/richard1992mv/api-afiliados/security/advisories/new)
(pestaña **Security → Report a vulnerability**). Solo los mantenedores ven el reporte.

Incluye, si puedes:

- Endpoint, componente o archivo afectado
- Pasos para reproducirlo (petición HTTP, payload) y el resultado observado
- Impacto estimado: qué podría hacer un atacante
- Versión o commit donde lo encontraste

**No incluyas datos personales reales** (documentos, correos, salarios). Usa datos ficticios: la API maneja
información protegida por la Ley 1581 de 2012 de protección de datos personales.

## Qué esperar

| Etapa | Plazo objetivo |
|---|---|
| Acuse de recibo | 3 días hábiles |
| Evaluación inicial y severidad (CVSS) | 10 días hábiles |
| Corrección de severidad crítica o alta | 30 días |
| Corrección de severidad media o baja | Próxima versión planificada |

Te mantendremos informado del avance. Cuando la corrección esté publicada, se divulgará en un
[security advisory](https://github.com/richard1992mv/api-afiliados/security/advisories), con crédito para quien
la reportó si lo desea.

## Alcance

**Dentro del alcance:** el código de este repositorio, su imagen Docker y la configuración del pipeline.

**Fuera del alcance:**

- Hallazgos ya documentados como riesgo aceptado o pendiente en el [README](README.md#riesgos-aceptados)
- Vulnerabilidades en dependencias sin un vector de explotación en esta API (repórtalas al proyecto de la dependencia)
- Ataques de denegación de servicio por volumen o ingeniería social

## Buena fe

No emprenderemos acciones contra investigaciones de buena fe que respeten esta política: que no accedan a datos
de terceros más allá de lo necesario para demostrar el fallo, que no degraden el servicio y que den un tiempo
razonable para corregir antes de divulgar.
