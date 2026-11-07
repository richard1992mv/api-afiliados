// Convención de commits del proyecto (README → Commits), validada por commitlint en cada PR.
// Parte de @commitlint/config-conventional y ajusta lo que difiere: tipos, alcances y largo del asunto.
export default {
  extends: ['@commitlint/config-conventional'],
  rules: {
    // Agrega "sec" (cambios de seguridad), que la configuración estándar no conoce.
    'type-enum': [2, 'always', ['feat', 'fix', 'test', 'refactor', 'docs', 'chore', 'ci', 'build', 'sec']],
    // El alcance es opcional, pero si se usa debe ser una de las capas o áreas del proyecto.
    'scope-enum': [2, 'always', ['domain', 'application', 'web', 'persistence', 'docker', 'pipeline']],
    'header-max-length': [2, 'always', 72],
  },
  // Dependabot escribe "build: Bump x from a to b" (mayúscula, en inglés) y no permite cambiarlo.
  // Se exceptúan solo esos mensajes; cualquier otro commit en mayúscula sigue fallando.
  ignores: [(mensaje) => /^(build|ci)(\(deps\))?: Bump \S+ from \S+ to \S+/.test(mensaje)],
};
