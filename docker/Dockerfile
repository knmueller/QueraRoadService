# Stage 1: Build the application
FROM gradle:8.7-jdk21-alpine AS build

# Set the working directory
WORKDIR /app

# Just copy everything to the build image
COPY ./ ./

# Build the application
RUN ./gradlew buildFatJar --no-daemon

# Stage 2: Run the application
FROM amazoncorretto:21-alpine

# Set the working directory
WORKDIR /app

# Copy the built application from the build stage
COPY --from=build /app/roadService/build/libs/roadService-all.jar app.jar

# Expose the application port
EXPOSE 8081
EXPOSE 8082

# Run the application
CMD ["java", "-jar", "app.jar"]