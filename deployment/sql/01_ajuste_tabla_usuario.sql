-- =====================================================================
-- 01_ajuste_tabla_usuario.sql
-- HU-01 Registrar usuarios: alinea la tabla usuario con los datos de la HU
-- Base: mydb | Esquema: public
-- El script es idempotente: se puede ejecutar varias veces sin error.
-- =====================================================================

BEGIN;

-- 1. Nuevas columnas que pide la HU (opcionales)
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS fecha_nacimiento DATE;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS direccion        VARCHAR(255);

-- 2. documento_identidad deja de ser obligatorio (se conserva el UNIQUE)
ALTER TABLE usuario ALTER COLUMN documento_identidad DROP NOT NULL;

-- 3. Regla de negocio de salario_base: entre 0 y 15.000.000
ALTER TABLE usuario DROP CONSTRAINT IF EXISTS usuario_salario_base_check;
ALTER TABLE usuario DROP CONSTRAINT IF EXISTS ck_usuario_salario_base_rango;
ALTER TABLE usuario ADD CONSTRAINT ck_usuario_salario_base_rango
    CHECK (salario_base BETWEEN 0 AND 15000000);

-- 4. Roles base del sistema (solo se insertan si no existen)
INSERT INTO rol (nombre, descripcion) VALUES
    ('ADMIN',   'Administrador del sistema'),
    ('ASESOR',  'Asesor que gestiona solicitudes de credito'),
    ('CLIENTE', 'Cliente que solicita creditos')
ON CONFLICT (nombre) DO NOTHING;

COMMIT;
