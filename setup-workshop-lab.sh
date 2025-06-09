#!/bin/bash
echo "=== Jesús María Workshop Analysis Lab Setup ==="

# Create project structure
mkdir -p jesus-maria-lab/{workshop-data,spark-apps,notebooks,init-scripts}
cd jesus-maria-lab 

# Download configuration files
cat > docker-compose.yml << 'EOF'
# [Insert the complete Docker Compose configuration above]
EOF

cat > Dockerfile.jupyter << 'EOF'
# [Insert the Jupyter Dockerfile above]
EOF

# Create requirements file
cat > requirements.txt << 'EOF'
pandas==1.3.3
numpy==1.21.2
matplotlib==3.4.3
seaborn==0.11.2
plotly==5.3.1
psycopg2-binary==2.9.1
sqlalchemy==1.4.23
geopandas==0.10.2
EOF

# Create Spark configuration
cat > spark-defaults.conf << 'EOF'
spark.sql.adaptive.enabled=true
spark.sql.adaptive.coalescePartitions.enabled=true
spark.sql.shuffle.partitions=4
spark.default.parallelism=4
spark.driver.memory=1g
spark.executor.memory=1g
spark.driver.extraClassPath=/usr/local/spark/jars/postgresql-42.7.4.jar
spark.executor.extraClassPath=/usr/local/spark/jars/postgresql-42.7.4.jar
EOF

# Download sample dataset (simulated)
cat > workshop-data/inscripciones_talleres_jesus_maria.csv << 'EOF'
fecha_corte,codigo_alumno,fecha_nacimiento,edad,sexo,taller,local,dias,horario,periodo,precio_jesus_maria,precio_publico_general,precio_total,departamento,provincia,distrito,ubigeo
2024-01-15,STU001,1995-03-15,29,F,Computación Básica,Centro Cívico,Lunes-Miércoles-Viernes,09:00-11:00,2024-I,80.00,120.00,80.00,LIMA,LIMA,JESUS MARIA,150116
2024-01-15,STU002,1988-07-22,36,M,Inglés Intermedio,Centro Cívico,Martes-Jueves,18:00-20:00,2024-I,100.00,150.00,100.00,LIMA,LIMA,LIMA,150101
# ... additional sample records
EOF

echo "Project structure created successfully!"
echo "Run: docker-compose up -d"
echo "Access Jupyter at: http://localhost:8888"
echo "Access Spark UI at: http://localhost:8080"