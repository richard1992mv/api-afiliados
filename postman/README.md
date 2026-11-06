# Pruebas Postman de la API afiliados

| Archivo | Contenido |
|---|---|
| `afiliados.postman_collection.json` | 38 peticiones en 7 carpetas, con pruebas (`pm.test`) |
| `afiliados-local.postman_environment.json` | `baseUrl` y `smmlv`. **Sin secretos**: los tokens se generan al ejecutar |

## 1. Levantar la API con el perfil `dev`

La carpeta *01 Tokens* obtiene los tokens de `POST /dev/token`, que solo existe con el perfil `dev`.

PowerShell:
```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"; docker compose up -d --build
```

Git Bash:
```bash
SPRING_PROFILES_ACTIVE=dev docker compose up -d --build
```

Para volver al modo normal, `docker compose up -d` en una terminal nueva (sin la variable).

## 2. Importar y ejecutar

1. Postman → **Import** → arrastrar los dos archivos `.json`.
2. Seleccionar el entorno **Afiliados local** (arriba a la derecha).
3. Clic derecho en la colección **API Afiliados** → **Run collection** → **Run**.
   Las carpetas se ejecutan en orden y cada una usa datos de la anterior (tokens, `afiliadoId`).

Desde la terminal, con el [Postman CLI](https://learning.postman.com/docs/postman-cli/postman-cli-overview/):
```bash
postman collection run postman/afiliados.postman_collection.json -e postman/afiliados-local.postman_environment.json
```

## Qué prueba cada carpeta

| Carpeta | Comprueba |
|---|---|
| 00 Públicos | health, OpenAPI (esquema Bearer) y Swagger UI responden sin token |
| 01 Tokens | emite tokens ADMIN y CONSULTA; rechaza roles inexistentes |
| 02 Autenticación | 401 sin token, token basura, token alterado, `alg: none` y token en la URL |
| 03 Flujo ADMIN | registrar → consultar → listar → actualizar (RN2) → inactivar → 422 (RN4) |
| 04 Validaciones | 409 duplicado (RN1), 400 de RN3 y de campos, 404, id inválido, 405 |
| 05 Rol CONSULTA | lee (200); no registra, actualiza, inactiva ni borra (403) |
| 06 Cabeceras | `nosniff`, `X-Frame-Options`, `Referrer-Policy`, `no-store`, sin cookie, HSTS solo por HTTPS |

Además, cada respuesta pasa reglas comunes: responde en menos de 2 s y, si es un error, es ProblemDetail sin trazas.

## Notas

- Cada ejecución **crea dos afiliados** de prueba (CC y TI con un documento aleatorio) en la base local.
- Si cambias `AFILIADOS_SMMLV` en la API, cambia también `smmlv` en el entorno: las pruebas de RN2 calculan la categoría esperada con ese valor.
- Sin el perfil `dev`, pega tokens válidos en las variables de colección `tokenAdmin` y `tokenConsulta` y omite la carpeta *01 Tokens*.
- El ataque con token vencido o firmado con otra llave no está aquí porque requiere conocer `JWT_SECRET`; lo cubren `JwtConfigTest` y `AfiliadosApplicationTests`.
