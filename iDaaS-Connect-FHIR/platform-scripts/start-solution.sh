cd kafka/non-windows
echo "Starting Kafka in background"
./start_kafka.sh &
cd ../../..
cd target
# Default connect to Kafka broker @localhost:9092
# ./solution-start.sh --idaas.kafkaBrokers=localhost:9092
JARFILE=`ls idaas-connect-fhir*.jar`
java -jar $JARFILE $@