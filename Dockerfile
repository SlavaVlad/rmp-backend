# Stage 1: Build (with cache)
FROM eclipse-temurin:21-jdk AS build

WORKDIR /home/gradle/project
COPY . .

RUN chmod +x gradlew && ./gradlew buildFatJar --no-daemon

# Stage 2: Runtime
FROM amazoncorretto:21
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
