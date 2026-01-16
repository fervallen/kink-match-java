FROM eclipse-temurin:21-jdk

WORKDIR /app

# Cache dependencies
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw -B dependency:go-offline

# Source code will be mounted via volume in docker-compose
EXPOSE 8080 5005

CMD ["./mvnw", "spring-boot:run"]
