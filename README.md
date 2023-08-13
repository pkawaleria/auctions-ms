# Auctions microservice

## How to run

First execute the docker-compose.yml script to setup a local MongoDB instance.
```bash
docker compose up -d
```

Then start the application via your IDE or executing gradlew command:
```bash
./gradlew bootRun
```

## Swagger docs
Navigate to
http://localhost:8080/swagger-ui/index.html# to see OpenAPI docs