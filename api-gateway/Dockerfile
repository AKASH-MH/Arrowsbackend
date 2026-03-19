FROM eclipse-temurin:17-jre
WORKDIR /app
COPY api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 3000
ENTRYPOINT ["java","-jar","/app/app.jar"]
