import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import java.util.Properties

object WorkshopAnalysisApp {
  
  def main(args: Array[String]): Unit = {
    val spark = createSparkSession()
    import spark.implicits._
    
    try {
      println("🌟 ANÁLISIS DE TALLERES MUNICIPALES JESÚS MARÍA 🌟")
      println("=" * 60)
      
      val workshopDF = loadWorkshopData(spark)
      
      // REQUERIMIENTO 2: MapReduce Operations
      performMapReduceAnalysis(spark, workshopDF)
      
      // REQUERIMIENTO 3: Spark SQL Operations
      performSparkSQLAnalysis(spark, workshopDF)
      
      // Guardar resultados
      saveResults(spark, workshopDF)
      
    } finally {
      spark.stop()
    }
  }
  
  def createSparkSession(): SparkSession = {
    SparkSession.builder()
      .appName("Jesús María Workshop Analysis")
      .config("spark.sql.adaptive.enabled", "true")
      .config("spark.sql.adaptive.coalescePartitions.enabled", "true")
      .config("spark.sql.shuffle.partitions", "4")
      .config("spark.default.parallelism", "4")
      .getOrCreate()
  }
  
  def loadWorkshopData(spark: SparkSession): DataFrame = {
    val schema = StructType(Array(
      StructField("fecha_corte", StringType, true),
      StructField("codigo_alumno", StringType, true),
      StructField("fecha_nacimiento", StringType, true),
      StructField("edad", IntegerType, true),
      StructField("sexo", StringType, true),
      StructField("taller", StringType, true),
      StructField("local", StringType, true),
      StructField("dias", StringType, true),
      StructField("horario", StringType, true),
      StructField("periodo", StringType, true),
      StructField("precio_jesus_maria", DoubleType, true),
      StructField("precio_publico_general", DoubleType, true),
      StructField("precio_total", DoubleType, true),
      StructField("departamento", StringType, true),
      StructField("provincia", StringType, true),
      StructField("distrito", StringType, true),
      StructField("ubigeo", StringType, true)
    ))
    
    spark.read
      .format("csv")
      .option("header", "true")
      .option("delimiter", ";")
      .option("encoding", "UTF-8")
      .schema(schema)
      .load("/home/jlorenzor/data/inscripciones_talleres_jesus_maria.csv")
  }
  
  def performMapReduceAnalysis(spark: SparkSession, df: DataFrame): Unit = {
    import spark.implicits._
    
    println("\n🔥 === ANÁLISIS MAPREDUCE === 🔥")
    
    // 2.1: Tres consultas con 3+ campos
    println("\n📊 2.1.1 Consulta Multi-campo: Demografía por Taller")
    df.groupBy($"taller", $"sexo", $"distrito")
      .agg(count("*").as("total_inscripciones"), avg($"edad").as("edad_promedio"))
      .orderBy(desc("total_inscripciones"))
      .show(10, truncate = false)
    
    println("\n📊 2.1.2 Consulta Multi-campo: Análisis Temporal-Geográfico")
    df.groupBy($"periodo", $"departamento", $"local")
      .agg(countDistinct($"codigo_alumno").as("estudiantes_unicos"), sum($"precio_total").as("ingresos_totales"))
      .show(10, truncate = false)
    
    println("\n📊 2.1.3 Consulta Multi-campo: Horarios y Precios")
    df.groupBy($"horario", $"dias", $"taller")
      .agg(avg($"precio_total").as("precio_promedio"), count("*").as("demanda"))
      .filter($"demanda" > 1)
      .show(10, truncate = false)
    
    // 2.2: Agrupar por tipos y encontrar max/min
    println("\n📊 2.2 Agrupación con Max/Min por Tipo de Taller")
    df.groupBy($"taller")
      .agg(count("*").as("total"), min($"edad").as("edad_min"), max($"edad").as("edad_max"))
      .orderBy(desc("total"))
      .show(10, truncate = false)
    
    // 2.3: Estadísticas completas
    println("\n📊 2.3 Análisis Estadístico Completo: PRECIO_TOTAL")
    df.agg(
      avg($"precio_total").as("promedio"),
      stddev($"precio_total").as("desviacion"),
      min($"precio_total").as("minimo"),
      max($"precio_total").as("maximo")
    ).show()
    
    // 2.4: Tres consultas con decimales
    println("\n📊 2.4.1 Consulta Decimal: Análisis de Descuentos")
    df.withColumn("descuento", $"precio_publico_general" - $"precio_jesus_maria")
      .withColumn("porcentaje_desc", round(($"descuento" / $"precio_publico_general") * 100, 2))
      .groupBy($"taller")
      .agg(avg($"porcentaje_desc").as("desc_promedio"))
      .show(10, truncate = false)
    
    println("\n📊 2.4.2 Consulta Decimal: Segmentación de Precios")
    df.withColumn("precio_norm", round($"precio_total" / 100.0, 3))
      .groupBy($"sexo")
      .agg(avg($"precio_norm").as("precio_normalizado"))
      .show()
    
    println("\n📊 2.4.3 Consulta Decimal: Eficiencia Geográfica")
    df.withColumn("precio_hora", round($"precio_total" / 8.0, 2))
      .groupBy($"distrito")
      .agg(avg($"precio_hora").as("costo_hora_avg"))
      .show(10, truncate = false)
  }
  
  def performSparkSQLAnalysis(spark: SparkSession, df: DataFrame): Unit = {
    println("\n🔥 === ANÁLISIS SPARK SQL === 🔥")
    
    df.createOrReplaceTempView("talleres")
    storeInPostgreSQL(spark, df)
    
    // 3.1: Consultas SQL básicas
    println("\n📊 3.1.1 Columnas específicas")
    spark.sql("SELECT codigo_alumno, taller, edad FROM talleres LIMIT 5").show()
    
    println("\n�� 3.1.2 Comando FILTER")
    spark.sql("SELECT taller, COUNT(*) as total FROM talleres WHERE edad > 25 GROUP BY taller").show()
    
    println("\n📊 3.1.3 Información ordenada")
    spark.sql("SELECT taller, precio_total FROM talleres ORDER BY precio_total DESC LIMIT 5").show()
    
    println("\n📊 3.1.4 GroupBy y Count")
    spark.sql("SELECT sexo, COUNT(*) as total FROM talleres GROUP BY sexo").show()
    
    println("\n📊 3.1.5 Consulta con promedio")
    spark.sql("SELECT taller, AVG(edad) as edad_avg FROM talleres GROUP BY taller").show(5)
    
    // 3.2: Funciones SQL avanzadas
    executeAdvancedSQLQueries(spark)
    
    // 3.3: Vistas temporales y JOINs
    executeTemporaryViewQueries(spark)
  }
  
  def executeAdvancedSQLQueries(spark: SparkSession): Unit = {
    import org.apache.spark.sql.functions._
    
    println("\n📊 3.2.1 Funciones SQL - Transformaciones")
    spark.table("talleres")
      .select(upper(col("sexo")).as("sexo_upper"), when(col("edad") < 30, "Joven").otherwise("Adulto").as("categoria"))
      .show(5)
    
    println("\n📊 3.2.2 Funciones SQL - Agregaciones")
    spark.table("talleres")
      .groupBy(col("distrito"))
      .agg(sum(col("precio_total")).as("ingresos"), avg(col("edad")).as("edad_avg"))
      .show(5)
    
    println("\n📊 3.2.3 Funciones SQL - Ventanas")
    import org.apache.spark.sql.expressions.Window
    val windowSpec = Window.partitionBy("taller").orderBy(desc("precio_total"))
    spark.table("talleres")
      .withColumn("ranking", row_number().over(windowSpec))
      .filter(col("ranking") <= 2)
      .show(10)
  }
  
  def executeTemporaryViewQueries(spark: SparkSession): Unit = {
    // Crear vistas temporales
    spark.sql("""
      CREATE OR REPLACE TEMPORARY VIEW resumen_demografico AS
      SELECT 
        CASE WHEN edad < 25 THEN 'Joven' ELSE 'Adulto' END as grupo_edad,
        sexo, COUNT(*) as conteo
      FROM talleres GROUP BY grupo_edad, sexo
    """)
    
    spark.sql("""
      CREATE OR REPLACE TEMPORARY VIEW resumen_talleres AS
      SELECT taller, COUNT(*) as inscripciones, AVG(precio_total) as precio_avg
      FROM talleres GROUP BY taller
    """)
    
    // 3.3.1: Tres JOINs
    println("\n📊 3.3.1.1 JOIN: Talleres y Demográfico")
    spark.sql("""
      SELECT t.taller, d.grupo_edad, d.conteo
      FROM talleres t
      JOIN resumen_demografico d ON 
        (CASE WHEN t.edad < 25 THEN 'Joven' ELSE 'Adulto' END = d.grupo_edad)
      LIMIT 10
    """).show()
    
    println("\n📊 3.3.1.2 JOIN: Resumen Talleres")
    spark.sql("""
      SELECT rt.taller, rt.inscripciones, t.codigo_alumno
      FROM resumen_talleres rt
      JOIN talleres t ON rt.taller = t.taller
      WHERE rt.inscripciones > 5
      LIMIT 10
    """).show()
    
    println("\n📊 3.3.1.3 JOIN: Auto-join Talleres")
    spark.sql("""
      SELECT t1.taller, t1.distrito, t2.distrito
      FROM talleres t1
      JOIN talleres t2 ON t1.taller = t2.taller AND t1.distrito != t2.distrito
      LIMIT 10
    """).show()
    
    // 3.3.2: Tres GroupBy con count
    println("\n📊 3.3.2.1 GroupBy Count: Edad y Sexo")
    spark.sql("""
      SELECT 
        CASE WHEN edad < 30 THEN '20-29' ELSE '30+' END as rango,
        sexo, COUNT(*) as cantidad
      FROM talleres GROUP BY rango, sexo
    """).show()
    
    println("\n📊 3.3.2.2 GroupBy Count: Talleres por Local")
    spark.sql("""
      SELECT local, COUNT(DISTINCT taller) as talleres_unicos
      FROM talleres GROUP BY local
    """).show()
    
    println("\n📊 3.3.2.3 GroupBy Count: Distribución Temporal")
    spark.sql("""
      SELECT periodo, distrito, COUNT(*) as inscripciones
      FROM talleres GROUP BY periodo, distrito
    """).show()
    
    // 3.3.3: Tres OrderBy combinados
    println("\n📊 3.3.3.1 OrderBy + Filter + Agregación")
    spark.sql("""
      SELECT taller, AVG(precio_total) as precio_avg, COUNT(*) as total
      FROM talleres
      WHERE edad BETWEEN 25 AND 45
      GROUP BY taller
      ORDER BY precio_avg DESC
    """).show(5)
    
    println("\n�� 3.3.3.2 OrderBy + Case + Agrupación")
    spark.sql("""
      SELECT 
        CASE WHEN precio_total < 80 THEN 'Bajo' ELSE 'Alto' END as categoria,
        COUNT(*) as cantidad
      FROM talleres
      GROUP BY categoria
      ORDER BY cantidad DESC
    """).show()
    
    println("\n📊 3.3.3.3 OrderBy + Having + Múltiples campos")
    spark.sql("""
      SELECT distrito, taller, COUNT(*) as inscripciones
      FROM talleres
      GROUP BY distrito, taller
      HAVING COUNT(*) > 1
      ORDER BY distrito, inscripciones DESC
    """).show(10)
  }
  
  // def storeInPostgreSQL(df: DataFrame): Unit = {
  //   val connectionProperties = new Properties()
  //   connectionProperties.put("user", "workshop_user")
  //   connectionProperties.put("password", "workshop_pass")
  //   connectionProperties.put("driver", "org.postgresql.Driver")
    
  //   val postgresUrl = "jdbc:postgresql://postgres:5432/jesus_maria_workshops"
    
  //   try {
  //     df.write.mode("overwrite").jdbc(postgresUrl, "workshop_enrollments", connectionProperties)
  //     println("✅ Datos almacenados en PostgreSQL")
  //   } catch {
  //     case e: Exception => println(s"⚠️ Error PostgreSQL: ${e.getMessage}")
  //   }
  // }

  def storeInPostgreSQL(spark: SparkSession, df: DataFrame): Unit = {
    import spark.implicits._
    
    val connectionProperties = new Properties()
    connectionProperties.put("user", "workshop_user")
    connectionProperties.put("password", "workshop_pass")
    connectionProperties.put("driver", "org.postgresql.Driver")
    
    val postgresUrl = "jdbc:postgresql://postgres:5432/jesus_maria_workshops"
    
    try {
      println("💾 Limpiando y almacenando datos en PostgreSQL...")
      
      // Limpiar datos antes de guardar
      val cleanDF = df
        .filter($"edad" >= 0 && $"edad" <= 120)
        .filter($"sexo".isin("M", "F", "MASCULINO", "FEMENINO"))
        .withColumn("fecha_corte_clean", 
          when($"fecha_corte".isNotNull && length($"fecha_corte") === 8, 
              to_date($"fecha_corte", "yyyyMMdd")).otherwise(lit(null).cast("date")))
        .withColumn("fecha_nacimiento_clean", 
          when($"fecha_nacimiento".isNotNull && length($"fecha_nacimiento") === 8, 
              to_date($"fecha_nacimiento", "yyyyMMdd")).otherwise(lit(null).cast("date")))
        .drop("fecha_corte", "fecha_nacimiento")
        .withColumnRenamed("fecha_corte_clean", "fecha_corte")
        .withColumnRenamed("fecha_nacimiento_clean", "fecha_nacimiento")
      
      cleanDF.write.mode("overwrite").jdbc(postgresUrl, "workshop_enrollments", connectionProperties)
      println("✅ Datos almacenados en PostgreSQL")
    } catch {
      case e: Exception => println(s"⚠️ Error PostgreSQL: ${e.getMessage}")
    }
  }

  def saveResults(spark: SparkSession, df: DataFrame): Unit = {
    import spark.implicits._
    
    val connectionProperties = new Properties()
    connectionProperties.put("user", "workshop_user")
    connectionProperties.put("password", "workshop_pass")
    connectionProperties.put("driver", "org.postgresql.Driver")
    
    val postgresUrl = "jdbc:postgresql://postgres:5432/jesus_maria_workshops"
    
    try {
      println("📊 Guardando análisis como tablas...")
      
      // 1. Análisis demográfico por taller
      val demographicAnalysis = df.groupBy("taller", "sexo")
        .agg(
          count("*").as("total_inscripciones"),
          avg("edad").as("edad_promedio"),
          min("edad").as("edad_minima"),
          max("edad").as("edad_maxima"),
          countDistinct("codigo_alumno").as("estudiantes_unicos")
        )
      
      // 2. Análisis de precios por taller
      val priceAnalysis = df.groupBy("taller")
        .agg(
          count("*").as("total_inscripciones"),
          avg("precio_total").as("precio_promedio"),
          min("precio_total").as("precio_minimo"),
          max("precio_total").as("precio_maximo"),
          avg("precio_jesus_maria").as("precio_residente_promedio"),
          avg("precio_publico_general").as("precio_publico_promedio")
        )
      
      // 3. Análisis geográfico
      val geographicAnalysis = df.groupBy("distrito", "local")
        .agg(
          count("*").as("total_inscripciones"),
          countDistinct("taller").as("variedad_talleres"),
          avg("precio_total").as("precio_promedio"),
          sum("precio_total").as("ingresos_totales")
        )
      
      // 4. Análisis temporal
      val temporalAnalysis = df.groupBy("periodo", "taller")
        .agg(
          count("*").as("inscripciones"),
          countDistinct("codigo_alumno").as("estudiantes_unicos"),
          avg("edad").as("edad_promedio")
        )
      
      // 5. Resumen general (ya existente mejorado)
      val summaryStats = df.groupBy("taller")
        .agg(
          count("*").as("total_inscripciones"),
          avg("edad").as("edad_promedio"),
          avg("precio_total").as("precio_promedio"),
          countDistinct("codigo_alumno").as("estudiantes_unicos"),
          countDistinct("local").as("locales_disponibles")
        )
      
      // Guardar todas las tablas
      demographicAnalysis.write.mode("overwrite")
        .jdbc(postgresUrl, "analisis_demografico", connectionProperties)
        
      priceAnalysis.write.mode("overwrite")
        .jdbc(postgresUrl, "analisis_precios", connectionProperties)
        
      geographicAnalysis.write.mode("overwrite")
        .jdbc(postgresUrl, "analisis_geografico", connectionProperties)
        
      temporalAnalysis.write.mode("overwrite")
        .jdbc(postgresUrl, "analisis_temporal", connectionProperties)
        
      summaryStats.write.mode("overwrite")
        .jdbc(postgresUrl, "analisis_resumen_talleres", connectionProperties)
      
      println("✅ Todas las consultas guardadas como tablas:")
      println("   - analisis_demografico")
      println("   - analisis_precios") 
      println("   - analisis_geografico")
      println("   - analisis_temporal")
      println("   - analisis_resumen_talleres")
      
    } catch {
      case e: Exception => println(s"⚠️ No se pudo guardar análisis: ${e.getMessage}")
    }
  }
}
