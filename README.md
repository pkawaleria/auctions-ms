# Auctions microservice

## How to run

First execute the docker-compose.yml script to setup a local MongoDB instance.
```bash
docker compose up -d
```

Then start the application via your IDE (preferable for development) or executing gradlew command:
```bash
./gradlew bootRun
```

## Swagger docs
Navigate to
http://localhost:8080/swagger-ui/index.html# to see OpenAPI docs

## Kafka [for the future]
Run the kafka tailored docker compose using command `docker-compose -f docker-compose-kafka.yml up -d`
To go into kafka control panel navigate to: http://localhost:9021/clusters
There you can manage kafka cluster, see messages inside topics etc. 
