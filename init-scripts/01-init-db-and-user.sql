-- ================================================================
-- SCRIPT 1: Inicialización de Base de Datos y Usuario
-- ================================================================

-- Crear usuario personalizado PRIMERO
CREATE USER workshop_user WITH PASSWORD 'workshop_pass';

-- Otorgar permisos de creación de BD
ALTER USER workshop_user CREATEDB;

-- Crear base de datos para talleres
CREATE DATABASE jesus_maria_workshops OWNER workshop_user;

-- Otorgar todos los permisos
GRANT ALL PRIVILEGES ON DATABASE jesus_maria_workshops TO workshop_user;

-- Conectar a la nueva base de datos
\c jesus_maria_workshops;

-- Otorgar permisos en el esquema public
GRANT ALL ON SCHEMA public TO workshop_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO workshop_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO workshop_user;

-- Establecer permisos por defecto para objetos futuros
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO workshop_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO workshop_user;

-- Confirmar la creación
\echo 'Base de datos jesus_maria_workshops creada exitosamente'
\echo 'Usuario workshop_user configurado correctamente'