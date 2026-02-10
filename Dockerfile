FROM gradle:8.8-jdk21 AS build
WORKDIR /app

COPY build.gradle* settings.gradle* ./
COPY gradle gradle
COPY src src

RUN gradle clean bootJar -x test


FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]