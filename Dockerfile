# syntax=docker/dockerfile:1.2

# First we need to tell docker which image we would like to use as the base container image6
FROM eclipse-temurin:17-jdk-jammy

# Create a folder to put the stuff we need for this image in
WORKDIR /application

# Because target is in .dockerignore put the most up to date jar at the root of your project folder for inclusion in Docker Build
COPY pprrank-0.1.0.jar ./

# Run the jar
CMD ["java", "-jar", "pprrank-0.1.0.jar"]