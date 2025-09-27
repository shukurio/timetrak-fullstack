# Use Maven with JDK 21 (Eclipse Temurin)
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

# Runtime JDK 21 (slim)
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Set timezone to Eastern
ENV TZ=America/Detroit
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8093
ENTRYPOINT ["java", "-Duser.timezone=America/Detroit", "-jar", "app.jar"]



