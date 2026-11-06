-- V1: tabla de afiliados.
-- Las restricciones CHECK repiten en la BD las reglas del dominio (defensa en profundidad):
-- protegen los datos aunque alguien escriba directamente en la tabla sin pasar por la API.

CREATE TABLE afiliados (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    tipo_documento   VARCHAR(2)    NOT NULL,
    numero_documento VARCHAR(20)   NOT NULL,
    nombres          VARCHAR(100)  NOT NULL,
    apellidos        VARCHAR(100)  NOT NULL,
    email            VARCHAR(254)  NOT NULL,
    salario          DECIMAL(15,2) NOT NULL,
    categoria        VARCHAR(1)    NOT NULL,
    estado           VARCHAR(10)   NOT NULL,

    CONSTRAINT pk_afiliados PRIMARY KEY (id),
    -- RN1: el documento (tipo + número) es único. Último control ante registros concurrentes.
    CONSTRAINT uk_afiliados_documento UNIQUE (tipo_documento, numero_documento),
    -- RN3: tipos de documento permitidos.
    CONSTRAINT ck_afiliados_tipo_documento CHECK (tipo_documento IN ('CC', 'CE', 'TI', 'PA')),
    CONSTRAINT ck_afiliados_categoria CHECK (categoria IN ('A', 'B', 'C')),
    CONSTRAINT ck_afiliados_estado CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT ck_afiliados_salario CHECK (salario > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
