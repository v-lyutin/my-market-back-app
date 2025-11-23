FROM amazoncorretto:21-alpine-jdk AS build

WORKDIR /app

COPY gradlew ./gradlew
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN chmod +x ./gradlew

COPY src ./src

RUN ./gradlew bootJar --no-daemon

FROM amazoncorretto:21-alpine-jdk AS runtime

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]