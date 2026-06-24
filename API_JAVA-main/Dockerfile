FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
ENV MAVEN_OPTS="-Xmx256m"
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/DoAnJava2-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=render", "-Xmx256m", "-jar", "app.jar"]
