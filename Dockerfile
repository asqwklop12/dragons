FROM eclipse-temurin:25-jre

WORKDIR /app

COPY build/libs/dragons-0.0.1-SNAPSHOT.jar dragons.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "dragons.jar"]
