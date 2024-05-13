FROM eclipse-temurin:17

LABEL maintainer="Graceman"

WORKDIR /app

COPY target/app-1.0-SNAPSHOT.jar /app/springboot-docker-demo.jar

ENTRYPOINT ["java", "-jar", "springboot-docker-demo.jar"]