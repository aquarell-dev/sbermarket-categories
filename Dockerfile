FROM maven:3.9-eclipse-temurin-22-alpine AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean install package

# Create a new stage for the runtime image
FROM eclipse-temurin:22-alpine as runtime

RUN mkdir /opt/app

COPY --from=build /app/target/ /opt/app/

WORKDIR /opt/app/

RUN mkdir /output

# Define the command to run the application
CMD ["java", "-jar", "sbermarket-1.0-SNAPSHOT-jar-with-dependencies.jar"]