docker-compose exec postgres psql -U workshop_user -d jesus_maria_workshops

# Análisis de Talleres Municipales Jesús María - Spark & Scala

Laboratorio 03 - CC531 A Análisis en Macrodatos 2025-1

## 📋 Descripción

Sistema de análisis de macrodatos para inscripciones virtuales de talleres municipales usando Apache Spark, Scala, y PostgreSQL.

**Dataset:** Inscripciones virtuales talleres municipales Jesús María (PNDA)
- **Registros:** 1,570 inscripciones
- **Período:** 2024 Semestre 2
- **Campos:** 17 columnas (demográficos, económicos, geográficos)

## 🚀 Inicio Rápido

### Prerrequisitos
```bash
# Requeridos
docker
docker-compose
```

### Levantar el Proyecto

```bash
# 1. Levantar servicios
docker-compose up -d

# 2. Verificar que todo funcione
./verify-setup.sh
```

## 🔧 Servicios y Puertos

| Servicio         | URL                   | Descripción            |
| ---------------- | --------------------- | ---------------------- |
| **Spark Master** | http://localhost:8080 | Interface web de Spark |
| **Jupyter Lab**  | http://localhost:8888 | Notebooks interactivos |
| **PostgreSQL**   | localhost:5432        | Base de datos          |

### Credenciales PostgreSQL
```
Host: localhost
Port: 5432
Database: jesus_maria_workshops
Username: workshop_user
Password: workshop_pass
```

## 📊 Ejecutar Análisis

### 1. Análisis con Jupyter (Python)
```bash
# Acceder a Jupyter
# URL: http://localhost:8888
# Abrir: notebooks/02-basic-analysis.ipynb
```

### 2. Análisis Completo (Scala)
```bash
# Entrar al contenedor
docker-compose exec jupyter-workshop bash

# Ir al directorio de aplicaciones
cd /home/jlorenzor/apps

# Compilar aplicación Scala
sbt clean assembly

# Ejecutar análisis
spark-submit --class WorkshopAnalysisApp \
  --master local[2] \
  --jars /usr/local/spark/jars/postgresql-42.7.4.jar \
  target/scala-2.12/jesus-maria-workshop-analysis-assembly-1.0.0.jar \
  2>&1 | while IFS= read -r line; do echo "$(date '+%Y-%m-%d %H:%M:%S') $line"; done > analisis_con_timestamp.log
```

## 📈 Análisis Implementados

### MapReduce Operations (Scala)
- ✅ **3 consultas multi-campo (3+ campos)**
  - Demografía por taller-sexo-distrito
  - Análisis temporal-geográfico-económico
  - Horarios-días-talleres
- ✅ **Agrupación con max/min por tipo**
- ✅ **Estadísticas completas** (promedio, mediana, desv. estándar)
- ✅ **3 consultas con decimales complejas**

### Spark SQL Operations
- ✅ **Almacenamiento en PostgreSQL**
- ✅ **Consultas básicas:** SELECT, FILTER, ORDER BY, GROUP BY
- ✅ **3 consultas con funciones avanzadas**
- ✅ **3 consultas JOIN con vistas temporales**
- ✅ **3 consultas GROUP BY + COUNT**
- ✅ **3 consultas ORDER BY combinadas**

## 🗄️ Verificar Datos en PostgreSQL

### Desde línea de comandos de tu maquina:
```bash
# Conectar a PostgreSQL
docker-compose exec postgres psql -U workshop_user -d jesus_maria_workshops

# Ver tablas
\dt

# Consultar datos
SELECT * FROM analisis_resumen_talleres;

# Salir
\q
```

## 🛠️ Solución de Problemas

### Error: PostgreSQL no conecta
```bash
# Verificar estado de contenedores
docker-compose ps

# Reiniciar PostgreSQL
docker-compose restart postgres

# Ver logs
docker-compose logs postgres
```

### Error: Compilación Scala
```bash
# Limpiar archivos duplicados
cd /home/jlorenzor/apps
find . -name "*checkpoint*" -delete
rm -rf src/main/scala/.ipynb_checkpoints/

# Recompilar
sbt clean assembly
```

### Error: Spark Master no disponible
```bash
# Usar modo local en lugar de cluster
spark-submit --class WorkshopAnalysisApp \
  --master local[2] \
  --jars /usr/local/spark/jars/postgresql-42.7.4.jar \
  target/scala-2.12/jesus-maria-workshop-analysis-assembly-1.0.0.jar
```

## 📁 Estructura del Proyecto

```
workshop-analysis/
├── compose.yml                 # Configuración Docker Compose
├── Dockerfile.jupyter          # Imagen Jupyter con Python para probar como usar Spark
├── requirements.txt            # Dependencias Python
├── spark-defaults.conf         # Configuración Spark
├── verify-setup.sh             # Script verificación inicial
├── init-scripts/               # Scripts inicialización PostgreSQL
│   ├── 01-init-db-and-user.sql
│   └── 02-create-tables-and-indexes.sql
├── spark-apps/                 # Aplicación Scala
│   ├── build.sbt
│   ├── project/plugins.sbt
│   └── src/main/scala/WorkshopAnalysisApp.scala
├── notebooks/                  # Jupyter notebooks
│   └── exploracion-via-pyspark.ipynb
├── workshop-data/              # Datos del proyecto
│   └── inscripciones_talleres_jesus_maria.csv
└── scripts/                    # Scripts de utilidad
    └── run-complete-analysis.sh
```

## 🎯 Resultados Principales

[Falta tipiar aquí los resultados principales del análisis realizado, como estadísticas clave, insights obtenidos, etc.]

## 🔄 Comandos Útiles

```bash
# Reiniciar todo
docker-compose down && docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f

# Limpiar volúmenes
docker-compose down -v

# Backup PostgreSQL
docker-compose exec postgres pg_dump -U workshop_user jesus_maria_workshops > backup.sql

# Restaurar backup
docker-compose exec -T postgres psql -U workshop_user jesus_maria_workshops < backup.sql
```

## 📚 Referencias

- [Dataset PNDA](https://www.datosabiertos.gob.pe)
- [Apache Spark Documentation](https://spark.apache.org/docs/latest/)
- [Scala Spark API](https://spark.apache.org/docs/latest/api/scala/)

---

**Autores:** Alexandra Yagi, Jhonny Lorenzo
**Curso:** CC531 A - Análisis en Macrodatos 2025-1  
**Fecha:** 10 de Junio de 2025