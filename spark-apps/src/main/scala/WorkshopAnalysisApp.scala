import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import java.util.Properties

object WorkshopAnalysisApp {
  
  def main(args: Array[String]): Unit = {
    val spark = createSparkSession()
    import spark.implicits._
    
    try {
      // Load workshop data
      val workshopDF = loadWorkshopData(spark)
      
      // Execute all required analyses
      println("=== JESÚS MARÍA WORKSHOP ENROLLMENT ANALYSIS ===")
      
      // MapReduce Operations
      performMapReduceAnalysis(spark, workshopDF)
      
      // Spark SQL Operations
      performSparkSQLAnalysis(spark, workshopDF)
      
      // Save results to PostgreSQL
      saveResults(workshopDF)
      
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
      StructField("fecha_corte", DateType, true),
      StructField("codigo_alumno", StringType, true),
      StructField("fecha_nacimiento", DateType, true),
      StructField("edad", IntegerType, true),
      StructField("sexo", StringType, true),
      StructField("taller", StringType, true),
      StructField("local", StringType, true),
      StructField("dias", StringType, true),
      StructField("horario", StringType, true),
      StructField("periodo", StringType, true),
      StructField("precio_jesus_maria", DecimalType(10,2), true),
      StructField("precio_publico_general", DecimalType(10,2), true),
      StructField("precio_total", DecimalType(10,2), true),
      StructField("departamento", StringType, true),
      StructField("provincia", StringType, true),
      StructField("distrito", StringType, true),
      StructField("ubigeo", StringType, true)
    ))
    
    spark.read
      .format("csv")
      .option("header", "true")
      .schema(schema)
      .load("/opt/workshop-data/inscripciones_talleres_jesus_maria.csv")
  }
  
  def performMapReduceAnalysis(spark: SparkSession, df: DataFrame): Unit = {
    import spark.implicits._
    
    println("\n=== MAPREDUCE ANALYSIS ===")
    
    // Query 1: Workshop popularity by demographics (3+ fields)
    println("\n1. Workshop Popularity by Age Group and Gender:")
    val demographicAnalysis = df
      .withColumn("age_group", 
        when($"edad" < 18, "Youth")
        .when($"edad" < 35, "Young Adult")
        .when($"edad" < 55, "Adult")
        .otherwise("Senior"))
      .groupBy($"taller", $"age_group", $"sexo")
      .agg(
        count("*").as("enrollment_count"),
        avg($"precio_total").as("avg_price"),
        countDistinct($"codigo_alumno").as("unique_students")
      )
      .orderBy(desc("enrollment_count"))
    
    demographicAnalysis.show(20, false) 
    
    // Query 2: Geographic enrollment patterns (3+ fields)
    println("\n2. Geographic Enrollment Distribution:")
    val geographicAnalysis = df
      .groupBy($"departamento", $"provincia", $"distrito")
      .agg(
        count("*").as("total_enrollments"),
        countDistinct($"codigo_alumno").as("unique_students"),
        countDistinct($"taller").as("workshop_variety"),
        avg($"precio_total").as("avg_price")
      )
      .orderBy(desc("total_enrollments"))
    
    geographicAnalysis.show(15, false)
    
    // Query 3: Temporal pricing trends (3+ fields)
    println("\n3. Monthly Pricing and Enrollment Trends:")
    val temporalAnalysis = df
      .withColumn("month", date_format($"fecha_corte", "yyyy-MM"))
      .groupBy($"month", $"taller", $"local")
      .agg(
        count("*").as("enrollments"),
        avg($"precio_jesus_maria").as("avg_resident_price"),
        avg($"precio_publico_general").as("avg_public_price"),
        avg($"precio_total").as("avg_total_price")
      )
      .orderBy($"month", desc("enrollments"))
    
    temporalAnalysis.show(20, false)
    
    // Statistical Analysis of Pricing Field
    println("\n4. Statistical Analysis of Pricing (precio_total):")
    val pricingStats = df.agg(
      avg($"precio_total").as("average"),
      expr("percentile_approx(precio_total, 0.5)").as("median"),
      stddev($"precio_total").as("standard_deviation"),
      min($"precio_total").as("minimum"),
      max($"precio_total").as("maximum")
    )
    
    pricingStats.show()
    
    // Group by workshop type and find max/min pricing
    println("\n5. Price Range by Workshop Type:")
    val workshopPricing = df
      .groupBy($"taller")
      .agg(
        count("*").as("enrollments"),
        min($"precio_total").as("min_price"),
        max($"precio_total").as("max_price"),
        avg($"precio_total").as("avg_price")
      )
      .orderBy(desc("enrollments"))
    
    workshopPricing.show(15, false)
  }
  
  def performSparkSQLAnalysis(spark: SparkSession, df: DataFrame): Unit = {
    // Register as temporary view
    df.createOrReplaceTempView("workshops")
    
    println("\n=== SPARK SQL ANALYSIS ===")
    
    // Store in PostgreSQL
    storeInPostgreSQL(df)
    
    // Various SQL queries
    executeSQLQueries(spark)
    
    // Advanced SQL with functions
    executeAdvancedSQLQueries(spark)
  }
  
  def storeInPostgreSQL(df: DataFrame): Unit = {
    val connectionProperties = new Properties()
    connectionProperties.put("user", "workshop_user")
    connectionProperties.put("password", "workshop_pass")
    connectionProperties.put("driver", "org.postgresql.Driver")
    
    val postgresUrl = "jdbc:postgresql://postgres:5432/jesus_maria_workshops"
    
    df.write
      .mode("overwrite")
      .jdbc(postgresUrl, "workshop_enrollments", connectionProperties) 
    
    println("Data successfully stored in PostgreSQL")
  } 
  
  def executeSQLQueries(spark: SparkSession): Unit = {
    println("\n6. SQL Query - Workshop Selection with Filters:")
    spark.sql("""
      SELECT taller, local, COUNT(*) as enrollments, AVG(edad) as avg_age
      FROM workshops 
      WHERE edad > 25 AND precio_total > 50
      GROUP BY taller, local
      ORDER BY enrollments DESC
    """).show(10, false)
    
    println("\n7. SQL Query - Ordered Results by Price:")
    spark.sql("""
      SELECT codigo_alumno, taller, edad, sexo, precio_total
      FROM workshops
      ORDER BY precio_total DESC, edad ASC
      LIMIT 15
    """).show(15, false)
    
    println("\n8. SQL Query - Group By Analysis:")
    spark.sql("""
      SELECT departamento, 
             COUNT(*) as total_enrollments,
             COUNT(DISTINCT codigo_alumno) as unique_students
      FROM workshops
      GROUP BY departamento
      HAVING COUNT(*) > 10
      ORDER BY total_enrollments DESC
    """).show()
  }
  
  def executeAdvancedSQLQueries(spark: SparkSession): Unit = {
    println("\n9. Advanced SQL with Built-in Functions:")
    
    // Using org.apache.spark.sql.functions
    import org.apache.spark.sql.functions._
    
    val advancedAnalysis = spark.table("workshops")
      .select(
        col("taller"),
        col("sexo"),
        when(col("edad") < 30, "Young").otherwise("Mature").as("age_category"),
        round(col("precio_total"), 2).as("rounded_price"),
        upper(col("distrito")).as("district_upper"),
        length(col("taller")).as("workshop_name_length")
      )
      .groupBy(col("taller"), col("age_category"))
      .agg(
        count("*").as("count"),
        avg("rounded_price").as("avg_price"),
        collect_list("district_upper").as("districts")
      )
      .orderBy(desc("count"))
    
    advancedAnalysis.show(15, false)
    
    // Temporary views for joins
    createTemporaryViews(spark)
    executeJoinQueries(spark)
  }
  
  def createTemporaryViews(spark: SparkSession): Unit = {
    // Create demographic summary view
    spark.sql("""
      CREATE OR REPLACE TEMPORARY VIEW demographic_summary AS
      SELECT 
        CASE WHEN edad < 18 THEN 'Youth'
             WHEN edad < 35 THEN 'Young Adult'  
             WHEN edad < 55 THEN 'Adult'
             ELSE 'Senior' END as age_group,
        sexo,
        COUNT(*) as group_count,
        AVG(precio_total) as avg_price
      FROM workshops
      GROUP BY age_group, sexo
    """)
    
    // Create workshop summary view
    spark.sql("""
      CREATE OR REPLACE TEMPORARY VIEW workshop_summary AS
      SELECT 
        taller,
        COUNT(*) as total_enrollments,
        AVG(precio_total) as avg_price,
        COUNT(DISTINCT local) as venue_count
      FROM workshops
      GROUP BY taller
    """)
    
    // Create geographic summary view
    spark.sql("""
      CREATE OR REPLACE TEMPORARY VIEW geographic_summary AS
      SELECT 
        departamento,
        provincia,
        COUNT(*) as enrollments,
        COUNT(DISTINCT taller) as workshop_variety
      FROM workshops
      GROUP BY departamento, provincia
    """)
  }
  
  def executeJoinQueries(spark: SparkSession): Unit = {
    println("\n10. Join Analysis - Workshop and Demographics:")
    spark.sql("""
      SELECT w.taller, w.avg_price, d.age_group, d.group_count
      FROM workshop_summary w
      JOIN demographic_summary d ON 1=1
      WHERE w.total_enrollments > 5 AND d.group_count > 3
      ORDER BY w.avg_price DESC
    """).show(15, false)
    
    println("\n11. Geographic and Workshop Join:")
    spark.sql("""
      SELECT g.departamento, g.provincia, w.taller, 
             g.enrollments, w.avg_price
      FROM geographic_summary g
      JOIN workshops orig ON g.departamento = orig.departamento
      JOIN workshop_summary w ON orig.taller = w.taller
      GROUP BY g.departamento, g.provincia, w.taller, g.enrollments, w.avg_price
      ORDER BY g.enrollments DESC
    """).show(15, false)
    
    println("\n12. Complex Multi-table Analysis:")
    spark.sql("""
      SELECT 
        ws.taller,
        COUNT(DISTINCT orig.departamento) as geographic_reach,
        ws.total_enrollments,
        ws.avg_price,
        AVG(orig.edad) as overall_avg_age
      FROM workshop_summary ws
      JOIN workshops orig ON ws.taller = orig.taller
      GROUP BY ws.taller, ws.total_enrollments, ws.avg_price
      HAVING geographic_reach > 1
      ORDER BY ws.total_enrollments DESC
    """).show(10, false)
  }
  
  def saveResults(df: DataFrame): Unit = {
    // Save analysis results
    val connectionProperties = new Properties()
    connectionProperties.put("user", "workshop_user")
    connectionProperties.put("password", "workshop_pass")
    connectionProperties.put("driver", "org.postgresql.Driver")
    
    val postgresUrl = "jdbc:postgresql://postgres:5432/jesus_maria_workshops"
    
    // Save summary statistics
    val summaryStats = df.groupBy("taller")
      .agg(
        count("*").as("total_enrollments"),
        avg("edad").as("avg_age"),
        avg("precio_total").as("avg_price"),
        countDistinct("codigo_alumno").as("unique_students")
      )
    
    summaryStats.write
      .mode("overwrite")
      .jdbc(postgresUrl, "workshop_analysis_results", connectionProperties)
    
    println("Analysis results saved to PostgreSQL")
  }
}