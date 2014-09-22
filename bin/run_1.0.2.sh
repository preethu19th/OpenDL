
spark-submit \
  --master $1 \
  --name "opendl" \
  --class org.gd.spark.opendl.example.spark.LRTest \
  target/opendl-example-0.0.1-SNAPSHOT-jar-with-dependencies.jar
