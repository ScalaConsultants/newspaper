# Newspaper

[Architecture diagram](https://www.draw.io/#G0B2OTKxXqvVOUYW1MSmJLQ05DOVU)

## Running

### Locally (no `docker-machine`)

``` Shell
export DOCKER_MACHINE_IP=127.0.0.1
docker-compose start
```

### Using `docker-machine`

``` Shell
eval $(docker-machine env default)
export DOCKER_MACHINE_IP=$(docker-machine ip default)
```

You should also set the `bootstrap.servers` value in `{mailer,analyzer}/src/main/resources/application.conf`, preferably by copying it as `environment.conf` so it's ignored by Git.

### Running `mailer`

``` Shell
cd mailer
sbt run
```

### Running `analyzer`

``` Shell
cd analyzer
sbt run
```

### Testing with simple producer

``` Shell
docker exec -it kafka /opt/kafka_2.12-0.10.2.1/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic newspaper-content
```

You may need to change the version string in `kafka_2.12-0.10.2.1`.

### Connecting to psql database

``` Shell
docker exec -it postgres psql -h postgres -U postgres
```