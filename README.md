Newsletter

### Useful commands

eval $(docker-machine env default)

export DOCKER_MACHINE_IP=$(docker-machine ip default)

docker exec -it {container id} bash

cd opt/kafka_2.11-0.10.1.1/bin/

./kafka-console-producer.sh --broker-list localhost:9092 --topic newspaper
