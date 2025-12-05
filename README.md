# Device API 🚀

A RESTful API for managing devices built with Spring Boot 3, PostgreSQL, and Docker. Features comprehensive testing with tests.

## 📋 Table of Contents

- [Quick Start](#-quick-start)
- [API Documentation](#-api-documentation)
- [Domain Model](#-domain-model)
- [Validation Rules](#-validation-rules)
- [Testing & Code Coverage](#-testing--code-coverage)
- [Docker Configuration](#-docker-configuration)
- [Database Schema](#-database-schema)
- [Configuration](#-configuration)
- [Error Handling](#-error-handling)

---

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker & Docker Compose

### 1. Clone the Repository
```bash
git clone https://github.com/LenaSava/devicesApi.git
cd devicesApi
```

### 2. Run with Docker Compose
```bash
# Build and start all services
docker-compose up --build

# The API will be available at:
# http://localhost:8080/device-service/v1/devices
# Swagger UI: http://localhost:8080/swagger-ui.html
```

**Docker configuration includes:**
- PostgreSQL database (postgres:16-alpine)
- Spring Boot application service
- Automatic health checks
- Persistent data storage

**Environment Variables:**
```
POSTGRES_DB=devices_db
POSTGRES_USER=admin
POSTGRES_PASSWORD=password
SPRING_PROFILES_ACTIVE=default
```

### 3. Run Locally (without Docker)
```bash
# Start PostgreSQL (if not using Docker)
docker-compose up postgres -d

# Update application.yml with your database credentials

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### 4. Run Tests
```bash
# Run all tests with coverage
mvn clean test

# View coverage report
open target/site/jacoco/index.html

```

---

## 📚 API Documentation

### Swagger UI
Once the application is running, visit:
- **Swagger UI:** http://localhost:8080/swagger-ui.html

### Endpoints Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/device-service/v1/devices` | Create a new device |
| GET | `/device-service/v1/devices` | Get all devices (supports filtering, pagination, sorting) |
| GET | `/device-service/v1/devices/{id}` | Get device by ID |
| PATCH | `/device-service/v1/devices/{id}` | Update device |
| DELETE | `/device-service/v1/devices/{id}` | Delete device |

### Query Parameters

| Name | Type     | Description |
|------|----------|-------------|
| `brand` | String   | Optional - Filter devices by brand |
| `state` | Enum     | Optional - Filter by state (AVAILABLE, IN_USE, INACTIVE) |
| `page` | int      | Optional - Page number (default: 0) |
| `size` | int      | Optional - Page size (default: 10) |
| `sort` | String   | Optional - Sort field and direction (e.g., "name,asc") |

### Example Requests

#### Create Device
```bash
curl -X POST http://localhost:8080/device-service/v1/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15 Pro",
    "brand": "Apple"
  }'
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "iPhone 15 Pro",
  "brand": "Apple",
  "state": "AVAILABLE",
  "creationTime": "2024-12-05T10:30:00Z"
}
```

#### Get All Devices (with pagination and sorting)
```bash
curl "http://localhost:8080/device-service/v1/devices?page=0&size=10&sort=name,asc"
```

#### Get Device by ID
```bash
curl http://localhost:8080/device-service/v1/devices/1
```

#### Get Devices by Brand
```bash
curl "http://localhost:8080/device-service/v1/devices?brand=Apple"
```

#### Get Devices by State
```bash
curl "http://localhost:8080/device-service/v1/devices?state=AVAILABLE"
```

#### Update
```bash
# Update only state
curl -X PATCH http://localhost:8080/device-service/v1/devices/1 \
  -H "Content-Type: application/json" \
  -d '{ "state": "INACTIVE" }'

# Update name and brand (only when device is not IN_USE)
curl -X PATCH http://localhost:8080/device-service/v1/devices/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15 Pro Max",
    "brand": "Apple Inc"
  }'
```

#### Delete Device
```bash
curl -X DELETE http://localhost:8080/device-service/v1/devices/1
```

---

## 🎯 Domain Model

### Device Entity
```json
{
  "id": "Long",
  "name": "String",
  "brand": "String",
  "state": "AVAILABLE | IN_USE | INACTIVE",
  "creationTime": "OffsetDateTime"
}
```

### Device States
- **AVAILABLE**: Device is ready to be used
- **IN_USE**: Device is currently being used
- **INACTIVE**: Device is not available for use

---

## ✅ Validation Rules

### On Create
- ✅ All fields (`name`, `brand`) are **required**
- ✅ `state` defaults to **AVAILABLE**
- ✅ `creationTime` is **auto-generated**

### On Update (PATCH)
- ❌ **Cannot update `name` or `brand` when device is IN_USE**
- ✅ Can always update `state`
- ❌ `creationTime` is **immutable** (cannot be updated)
- ✅ Supports partial updates (send only fields to change)

### On Delete
- ❌ **Cannot delete devices with state IN_USE**
- ✅ Can delete AVAILABLE or INACTIVE devices
- ✅ Returns 204 No Content on success

---

## 🧪 Testing & Code Coverage

### Test Suite Overview

The Device API includes **200+ comprehensive tests** with **85%+ code coverage** tracked by JaCoCo.

| Test Type        | Count    | Speed | Docker Required | Coverage |
|------------------|----------|-------|-----------------|----------|
| Unit Tests       | 170+     | ~10s | ❌ No            | Controllers, Services, Validators |
| Functional Tests | 30+      | ~45s | ✅ Yes           | End-to-End with real PostgreSQL |
| **Total**        | **200+** | **~55s** | -               | **85%+** |

### Quick Test Commands

```bash
# Run all tests with coverage
mvn clean test

# View JaCoCo coverage report
open target/site/jacoco/index.html

# Run specific test class
mvn test -Dtest=DeviceControllerTest

# Skip tests during build
mvn clean install -DskipTests
```

### JaCoCo Coverage Report

After running tests, open the interactive coverage report:

```bash
mvn clean test
open target/site/jacoco/index.html
```

**Report shows:**
- 📊 **Line Coverage** - Which lines were executed
- 🌳 **Branch Coverage** - Which if/else paths were taken
- 🎯 **Method Coverage** - Which methods were called
- 📦 **Package View** - Coverage organized by package

**Color Coding:**
- 🟢 **Green** = Fully covered
- 🟡 **Yellow** = Partially covered
- 🔴 **Red** = Not covered

### Test Prerequisites

**For Unit Tests:**
- ✅ Java 21+
- ✅ Maven 3.9+
- ❌ No Docker required

**For Functional Tests:**
- ✅ Java 21+
- ✅ Maven 3.9+
- ✅ **Docker Desktop running**
- ✅ Internet (first run downloads PostgreSQL image)

```bash
# Check Docker is running
docker ps
```

### Test Structure

```
src/test/java/org/hometask/devicesapi/
├── controller/
│   └── DeviceControllerTest.java          
├── service/
│   └── DeviceServiceImplTest.java         
├── validation/
│   └── DeviceValidatorTest.java           
└── functional/
    └── DeviceApiFunctionalTestSeparate.java 
```

### Coverage Goals

| Metric                  | Target | Current Status |
|-------------------------|--------|----------------|
| Overall Line Coverage   | 85%+ | ✅ ~90% |
| Overall Branch Coverage | 75%+ | ✅ ~85% |
| Controllers             | 90%+ | ✅ 92% |
| Services                | 95%+ | ✅ 96% |
| Validators              | 100% | ✅ 100% |

### CI/CD Integration

Tests are designed to run in continuous integration pipelines:

**GitHub Actions:**
```yaml
- name: Run tests with coverage
  run: mvn clean test
```

**GitLab CI:**
```yaml
test:
  script:
    - mvn clean test
```

**Jenkins:**
```groovy
sh 'mvn clean test'
```

## 🐳 Docker Configuration

### docker-compose.yml

The project includes a complete Docker Compose setup with:

**Services:**
1. **PostgreSQL Database**
    - Image: `postgres:16-alpine`
    - Port: `5432:5432`
    - Health checks enabled
    - Persistent data storage

2. **Spring Boot Application**
    - Built from local Dockerfile
    - Port: `8080:8080`
    - Depends on PostgreSQL health check
    - Auto-restart enabled

**Docker Commands:**

```bash
# Start all services
docker-compose up --build

# Start in background
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Stop and remove volumes (clean database)
docker-compose down -v

# Restart a specific service
docker-compose restart app
```

### Environment Variables

**PostgreSQL:**
```
POSTGRES_DB=devices_db
POSTGRES_USER=admin
POSTGRES_PASSWORD=password
```

**Application:**
```
DB_HOST=postgres
DB_PORT=5432
DB_NAME=devices_db
DB_USERNAME=admin
DB_PASSWORD=password
SPRING_PROFILES_ACTIVE=default
```

### Volumes
- `postgres_data`: Persists PostgreSQL data between container restarts

### Networks
- `devices-network`: Bridge network for service communication

---

## 📝 Database Schema

```sql
CREATE TABLE device (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(255) NOT NULL,
    state VARCHAR(20) NOT NULL,
    creation_time TIMESTAMP WITH TIME ZONE NOT NULL
);
```

### Database Access

When using Docker Compose, connect to the database:

```bash
# Using docker-compose
docker-compose exec postgres psql -U admin -d devices_db

# Using psql client locally
psql -h localhost -p 5432 -U admin -d devices_db
```

---

## 🔧 Configuration

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:devices_db}
    username: ${DB_USERNAME:admin}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 8080

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### Profiles

- **default**: Standard configuration with environment variable support
- **test**: Test configuration with H2 in-memory database

---

## 🚨 Error Handling

The API provides clear, consistent error responses:

### Example Error Responses

**400 Bad Request:**
```json
{
  "timestamp": "2024-12-05T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot update name of a device that is IN_USE",
  "path": "/device-service/v1/devices/1"
}
```

**404 Not Found:**
```json
{
  "timestamp": "2024-12-05T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Device not found with id: 999",
  "path": "/device-service/v1/devices/999"
}
```

### HTTP Status Codes

| Status | Description | Example |
|--------|-------------|---------|
| 200 OK | Successful GET or PATCH | Device retrieved/updated |
| 201 Created | Device created successfully | POST request succeeded |
| 204 No Content | Device deleted successfully | DELETE request succeeded |
| 400 Bad Request | Invalid input or business rule violation | Update IN_USE device name |
| 404 Not Found | Resource not found | Device ID doesn't exist |
| 500 Internal Server Error | Unexpected server error | Database connection failure |

---

## 📊 Health Check

Check application and database health:

```bash
curl http://localhost:8080/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

---

## 🔍 Troubleshooting

### Application Won't Start

**Check Docker:**
```bash
docker ps
docker-compose logs app
```

**Check Database Connection:**
```bash
docker-compose logs postgres
docker-compose exec postgres psql -U admin -d devices_db
```

### Tests Fail

**For Unit Tests:**
```bash
# Check Java version
java -version  # Should be Java 21

# Clean and rebuild
mvn clean test
```

**For Functional Tests:**
```bash
# Check Docker is running
docker ps

# Pull PostgreSQL image
docker pull postgres:16-alpine
```

### Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Write tests for new features
4. Ensure all tests pass (`mvn clean test`)
5. Check code coverage (`open target/site/jacoco/index.html`)
6. Commit your changes (`git commit -m 'Add amazing feature'`)
7. Push to the branch (`git push origin feature/amazing-feature`)
8. Open a Pull Request

### Code Quality Standards

- ✅ All new code must have tests
- ✅ Maintain 85%+ code coverage
- ✅ Follow existing code style
- ✅ Update documentation as needed
- ✅ All tests must pass before merging

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Savinkova Olena**

---

## 🙏 Acknowledgments

- Spring Boot Team
- PostgreSQL Community
- Docker Community
- Testcontainers Project
- JaCoCo Code Coverage Team

---
