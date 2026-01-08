# Intake Service

[**← Back to Main Architecture**](https://github.com/Macro-Tracker-Platform)

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-000?style=for-the-badge&logo=apachekafka)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

---

[![License](https://img.shields.io/badge/license-Apache%202.0-blue?style=for-the-badge)](LICENSE)
[![Swagger](https://img.shields.io/badge/Swagger-API_Docs-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](https://macrotracker.uk/webjars/swagger-ui/index.html?urls.primaryName=intake-service)
[![Docker Hub](https://img.shields.io/badge/Docker%20Hub-Image-blue?style=for-the-badge&logo=docker)](https://hub.docker.com/repository/docker/olehprukhnytskyi/macro-tracker-intake-service/general)

**Core Nutrition Tracking & Calculation Service.**

Responsible for high-throughput meal logging, nutritional value calculations (calories, macros), and historical data management.

## :zap: Service Specifics

* **Smart Batch Deletion (Recursive Event Pattern)**: To prevent database locks and Kafka consumer timeouts during GDPR data cleanup (e.g., deleting a user with years of logs):
    * The service deletes records in small batches (1000 items).
    * If more data exists, it republishes a `user-deleted` event to Kafka to process the next batch asynchronously.
* **Automatic Nutrition Calculation**: Fetches raw food data from **Food Service** and dynamically calculates values (Calories, Proteins, Fats, Carbs) based on the consumed grams.
* **Resilience & Fault Tolerance**: Implements `@Retryable` logic for external calls to **Food Service** to handle transient network failures gracefully.
* **Idempotency**: Protects `POST` creation endpoints with custom `@Idempotent` aspects to prevent duplicate entries during network retries.
* **Granular Caching**: Uses **Redis** to cache paginated daily logs. Cache is automatically invalidated (`@CacheEvict`) upon updates or new entries.

---

## :electric_plug: API & Communication

* **Sync Communication**:
    * Uses **OpenFeign** to fetch product details from **Food Service** (`GET /api/foods/{id}`).
* **Async Communication (Kafka)**:
    * **Consumes**: `user-deleted` topic (from User Service).
    * **Produces**: `user-deleted` topic (Loopback for recursive batch deletion).

---

## :hammer_and_wrench: Tech Details

| Component | Implementation |
| :--- | :--- |
| **Framework** | Spring Boot 3, Java 21 |
| **Database** | PostgreSQL, Liquibase |
| **Caching** | Redis (Time-to-Live + Custom Eviction Keys) |
| **Messaging** | Apache Kafka (`Spring Kafka`) |
| **API Client** | Spring Cloud OpenFeign, Spring Retry |
| **Testing** | JUnit 5, Testcontainers (Redis/PostgreSQL) |

---

## :gear: Environment Variables

Required variables for `local` or `k8s` deployment:

| Variable | Purpose |
| :--- | :--- |
| **Database** | |
| `DB_HOST` | Database hostname (e.g., `postgres`). |
| `DB_PORT` | Database port (e.g., `5432`). |
| `DB_NAME` | Database name. |
| `DB_USERNAME` | Database user. |
| `DB_PASSWORD` | Database password. |
| **Integrations** | |
| `FOOD_SERVICE_URL` | URL of the internal **Food Service** (e.g., `http://food-service:8080`). |
| `REDIS_URL` | Redis connection URL for caching. |
| `KAFKA_URL` | Kafka bootstrap servers address. |
| **Application** | |
| `MACRO_TRACKER_URL` | Public URL of the application (for Swagger). |

---

## :whale: Quick Start

```bash
# Pull from Docker Hub
docker pull olehprukhnytskyi/macro-tracker-intake-service:latest

# Run (Ensure your .env file contains all required variables)
docker run -p 8080:8080 --env-file .env olehprukhnytskyi/macro-tracker-intake-service:latest
```

---

## :balance_scale: License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.