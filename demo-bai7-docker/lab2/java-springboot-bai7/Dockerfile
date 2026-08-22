# Lab 2 — Dockerfile MULTI-STAGE
# Stage 1 (builder): Maven + JDK 17 biên dịch JAR trong image
# Stage 2 (runtime): chỉ JRE + JAR → image nhỏ, không kèm Maven
#
# Khác Lab 1: KHÔNG cần ./mvnw package trên máy trước docker build.
# target/ được ignore (build xảy ra trong container builder).

# --- Stage 1: build JAR ---
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom + wrapper TRƯỚC src → Docker cache layer dependency khi chỉ sửa Java
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy mã nguồn rồi package
COPY src ./src
RUN ./mvnw package -DskipTests -B

# --- Stage 2: chạy ---
FROM eclipse-temurin:17-jre
WORKDIR /app

# Chỉ lấy JAR từ stage builder — không mang theo Maven / source
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
