# -------- build stage --------
FROM gradle:8.7-jdk17 AS build
WORKDIR /workspace

# 캐시 최적화(가능하면 settings.gradle, build.gradle 먼저 복사)
COPY gradle gradle
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle
COPY src src

RUN gradle clean bootJar -x test

# -------- run stage --------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
