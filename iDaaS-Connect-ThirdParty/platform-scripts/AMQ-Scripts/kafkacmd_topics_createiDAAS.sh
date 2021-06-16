kafkaDir=$HOME'/RedHatTech/kafka_2.12-2.5.0.redhat-00003'
echo "Directory: "$kafkaDir
cd $kafkaDir
## Operational Topics for Platform
bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic opsmgmt_platformtransactions &

# Safe
bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic fhirsvr_adverseevent &
bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic ent_fhirsvr_adverseevent &