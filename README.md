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


## Metrics
> Metrics are collected from the running application by time series database Prometheus and visualized by Grafana interface. 

To view microservice metrics enter http://localhost:3001 on local machine. To log in for the first time use user 'admin' and password 'admin'
Then go to Dashboards section, click 'NEW' and either select option 'Import via grafana.com' entering  `11378` code or import the
json file `./grafana/dashboard.json`