# WinWin.travel Backend Test Task

## Architecture
- **Service A (auth-api):** Spring Boot (Web + Security + JPA), connects to Postgres, exposes endpoints for register/login and a protected /process endpoint. Saves logs.
- **Service B (data-api):** Spring Boot (Web), exposes /transform endpoint. Only accepts requests from Service A via shared header.
- **Postgres:** stores users and processing_log.

## How to Build and Run

1) Build and start the containers:
   
   mvn -f auth-api/pom.xml clean package -DskipTests
   
   mvn -f data-api/pom.xml clean package -DskipTests
   
   docker compose up -d --build

## How to Test

1) Register:
   curl -X POST http://localhost:8080/api/auth/register -H 'Content-Type: application/json' -d '{"email":"a@a.com","password":"pass"}'

2) Login:
   curl -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{"email":"a@a.com","password":"pass"}'

3) Process:
   curl -X POST http://localhost:8080/api/process -H 'Authorization: Bearer <token>' -H 'Content-Type: application/json' -d '{"text":"hello"}'
