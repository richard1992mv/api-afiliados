---
description: Crea un commit con Conventional Commits
---
1. Ejecuta git status y git diff --staged. Si no hay nada en stage, muéstrame los cambios y pregúntame qué agregar.
2. Verifica que no haya secretos, archivos .env ni carpetas target/ en el stage.
3. Ejecuta ./mvnw -q test; si falla, no hagas commit y explícame el error.
4. Propón un mensaje Conventional Commits en español:
   tipo(alcance): descripción en imperativo, máximo 72 caracteres
   Tipos: feat, fix, test, refactor, docs, chore, ci, build, sec
   Alcance: domain, application, web, persistence, docker, pipeline
   Si hace falta, un cuerpo que explique el porqué.
5. Muéstrame el mensaje y espera mi aprobación antes de hacer commit.
6. Nunca hagas push ni commit directo a main.
