-- ================================================================
-- SCRIPT 2: Creación de Tablas, Índices y Vistas
-- ================================================================

-- Conectar a la base de datos de talleres
\c jesus_maria_workshops workshop_user;

-- Crear tabla principal de inscripciones
CREATE TABLE IF NOT EXISTS workshop_enrollments (
    id BIGSERIAL PRIMARY KEY,
    fecha_corte DATE NOT NULL,
    codigo_alumno VARCHAR(50) NOT NULL,
    fecha_nacimiento DATE,
    edad INTEGER CHECK (edad >= 0 AND edad <= 120),
    sexo VARCHAR(20) CHECK (sexo IN ('M', 'F', 'Masculino', 'Femenino')),
    taller VARCHAR(200) NOT NULL,
    local VARCHAR(200),
    dias VARCHAR(100),
    horario VARCHAR(100),
    periodo VARCHAR(50),
    precio_jesus_maria DECIMAL(10,2) CHECK (precio_jesus_maria >= 0),
    precio_publico_general DECIMAL(10,2) CHECK (precio_publico_general >= 0),
    precio_total DECIMAL(10,2) CHECK (precio_total >= 0),
    departamento VARCHAR(100),
    provincia VARCHAR(100),
    distrito VARCHAR(100),
    ubigeo VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear índices para optimización de consultas
CREATE INDEX IF NOT EXISTS idx_workshop_taller ON workshop_enrollments(taller);
CREATE INDEX IF NOT EXISTS idx_workshop_fecha_corte ON workshop_enrollments(fecha_corte);
CREATE INDEX IF NOT EXISTS idx_workshop_codigo_alumno ON workshop_enrollments(codigo_alumno);
CREATE INDEX IF NOT EXISTS idx_workshop_edad_sexo ON workshop_enrollments(edad, sexo);
CREATE INDEX IF NOT EXISTS idx_workshop_ubigeo ON workshop_enrollments(ubigeo);
CREATE INDEX IF NOT EXISTS idx_workshop_periodo ON workshop_enrollments(periodo);

-- Índices compuestos para consultas complejas
CREATE INDEX IF NOT EXISTS idx_workshop_demographics ON workshop_enrollments(taller, edad, sexo);
CREATE INDEX IF NOT EXISTS idx_workshop_geographic ON workshop_enrollments(departamento, provincia, distrito);
CREATE INDEX IF NOT EXISTS idx_workshop_pricing ON workshop_enrollments(precio_jesus_maria, precio_publico_general, precio_total);

-- Tabla para almacenar resultados de análisis
CREATE TABLE IF NOT EXISTS analisis_resumen_talleres (
    id BIGSERIAL PRIMARY KEY,
    taller VARCHAR(200) NOT NULL,
    total_inscripciones INTEGER,
    edad_promedio DECIMAL(5,2),
    precio_promedio DECIMAL(10,2),
    estudiantes_unicos INTEGER,
    fecha_analisis TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Vista resumen por talleres
CREATE OR REPLACE VIEW enrollment_summary AS
SELECT 
    taller,
    COUNT(*) as total_enrollments,
    COUNT(DISTINCT codigo_alumno) as unique_students,
    ROUND(AVG(edad), 2) as avg_age,
    COUNT(CASE WHEN sexo IN ('M', 'Masculino') THEN 1 END) as male_count,
    COUNT(CASE WHEN sexo IN ('F', 'Femenino') THEN 1 END) as female_count,
    ROUND(AVG(precio_total), 2) as avg_price,
    MIN(precio_total) as min_price,
    MAX(precio_total) as max_price
FROM workshop_enrollments
GROUP BY taller
ORDER BY total_enrollments DESC;

-- Vista distribución geográfica
CREATE OR REPLACE VIEW geographic_distribution AS
SELECT 
    departamento,
    provincia,
    distrito,
    ubigeo,
    COUNT(*) as enrollments,
    COUNT(DISTINCT codigo_alumno) as unique_students,
    COUNT(DISTINCT taller) as workshop_variety,
    ROUND(AVG(precio_total), 2) as avg_price
FROM workshop_enrollments
GROUP BY departamento, provincia, distrito, ubigeo
ORDER BY enrollments DESC;

-- Vista análisis demográfico
CREATE OR REPLACE VIEW demographic_analysis AS
SELECT 
    CASE 
        WHEN edad < 18 THEN 'Youth (< 18)'
        WHEN edad < 25 THEN 'Young Adult (18-24)'
        WHEN edad < 35 THEN 'Adult (25-34)'
        WHEN edad < 50 THEN 'Middle Age (35-49)'
        ELSE 'Senior (50+)'
    END as age_group,
    sexo,
    COUNT(*) as count,
    ROUND(AVG(precio_total), 2) as avg_price,
    COUNT(DISTINCT taller) as workshops_variety
FROM workshop_enrollments
GROUP BY age_group, sexo
ORDER BY age_group, sexo;

\echo 'Tablas, índices y vistas creados exitosamente'