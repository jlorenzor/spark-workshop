CREATE DATABASE jesus_maria_workshops;

\c jesus_maria_workshops;

-- Workshop enrollments table matching dataset structure
CREATE TABLE workshop_enrollments (
    id BIGSERIAL PRIMARY KEY,
    fecha_corte DATE NOT NULL,
    codigo_alumno VARCHAR(50) NOT NULL,
    fecha_nacimiento DATE,
    edad INTEGER CHECK (edad >= 0 AND edad <= 120),
    sexo VARCHAR(10) CHECK (sexo IN ('M', 'F', 'Masculino', 'Femenino')),
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance optimization
CREATE INDEX idx_workshop_taller ON workshop_enrollments(taller);
CREATE INDEX idx_workshop_fecha_corte ON workshop_enrollments(fecha_corte);
CREATE INDEX idx_workshop_codigo_alumno ON workshop_enrollments(codigo_alumno);
CREATE INDEX idx_workshop_edad_sexo ON workshop_enrollments(edad, sexo);
CREATE INDEX idx_workshop_ubigeo ON workshop_enrollments(ubigeo);
CREATE INDEX idx_workshop_periodo ON workshop_enrollments(periodo); 

-- Composite indexes for complex queries
CREATE INDEX idx_workshop_demographics ON workshop_enrollments(taller, edad, sexo);
CREATE INDEX idx_workshop_geographic ON workshop_enrollments(departamento, provincia, distrito);
CREATE INDEX idx_workshop_pricing ON workshop_enrollments(precio_jesus_maria, precio_publico_general, precio_total);

-- Views for common analysis patterns
CREATE VIEW enrollment_summary AS
SELECT 
    taller,
    COUNT(*) as total_enrollments,
    COUNT(DISTINCT codigo_alumno) as unique_students,
    AVG(edad) as avg_age,
    COUNT(CASE WHEN sexo IN ('M', 'Masculino') THEN 1 END) as male_count,
    COUNT(CASE WHEN sexo IN ('F', 'Femenino') THEN 1 END) as female_count,
    AVG(precio_total) as avg_price,
    MIN(precio_total) as min_price,
    MAX(precio_total) as max_price
FROM workshop_enrollments
GROUP BY taller;

CREATE VIEW geographic_distribution AS
SELECT 
    departamento,
    provincia,
    distrito,
    ubigeo,
    COUNT(*) as enrollments,
    COUNT(DISTINCT codigo_alumno) as unique_students,
    COUNT(DISTINCT taller) as workshop_variety,
    AVG(precio_total) as avg_price
FROM workshop_enrollments
GROUP BY departamento, provincia, distrito, ubigeo;

-- Sample data insertion (for testing)
INSERT INTO workshop_enrollments 
(fecha_corte, codigo_alumno, fecha_nacimiento, edad, sexo, taller, local, dias, horario, periodo, 
 precio_jesus_maria, precio_publico_general, precio_total, departamento, provincia, distrito, ubigeo)
VALUES 
('2024-01-15', 'STU001', '1995-03-15', 29, 'F', 'Computación Básica', 'Centro Cívico', 'Lunes-Miércoles-Viernes', '09:00-11:00', '2024-I', 80.00, 120.00, 80.00, 'LIMA', 'LIMA', 'JESUS MARIA', '150116'),
('2024-01-15', 'STU002', '1988-07-22', 36, 'M', 'Inglés Intermedio', 'Centro Cívico', 'Martes-Jueves', '18:00-20:00', '2024-I', 100.00, 150.00, 100.00, 'LIMA', 'LIMA', 'LIMA', '150101'),
('2024-01-16', 'STU003', '2001-11-03', 23, 'F', 'Danza Folklórica', 'Casa de la Cultura', 'Sábados', '10:00-12:00', '2024-I', 60.00, 90.00, 60.00, 'LIMA', 'LIMA', 'JESUS MARIA', '150116');