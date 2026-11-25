# Linkly - URL shortener with QR codes
Java 21 / Spring Boot 3 / JPA / PostgreSQL (Neon) / ZXing / Docker / Render

## Run locally (H2 in-memory DB)
    mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=local
Open http://localhost:8080

## Deploy
1. Create a Neon project; note host, database, user, password.
2. Push this folder to GitHub.
3. Render -> New Web Service -> connect repo -> Runtime: Docker.
4. Environment variables:
   - DB_URL      = jdbc:postgresql://<neon-host>/<db>?sslmode=require
   - DB_USER     = <neon user>
   - DB_PASSWORD = <neon password>
