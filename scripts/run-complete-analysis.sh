#!/bin/bash

echo "🌟 Ejecutando Análisis Completo de Talleres Jesús María 🌟"
echo "========================================================="

# Compilar aplicación Scala
echo "📦 Compilando aplicación Scala..."
cd /opt/spark-apps
sbt clean assembly

# Ejecutar análisis en Spark
echo "🔥 Ejecutando análisis MapReduce y SQL..."
spark-submit \
  --class WorkshopAnalysisApp \
  --master spark://spark-master:7077 \
  --driver-memory 1g \
  --executor-memory 1g \
  --jars /usr/local/spark/jars/postgresql-42.7.4.jar \
  target/scala-2.12/jesus-maria-workshop-analysis-assembly-1.0.0.jar

echo "✅ Análisis completado. Revisa los resultados en PostgreSQL."