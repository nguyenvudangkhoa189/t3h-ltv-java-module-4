# Lab 1 — Dockerfile MỘT stage
# Yêu cầu: chạy ./mvnw package -DskipTests TRƯỚC khi docker build
# (JAR đã có trong target/; image chỉ chứa JRE + JAR)

FROM eclipse-temurin:17-jre

# --- Thư mục làm việc trong container ---
WORKDIR /app

# --- Copy JAR đã build trên máy HV vào image ---
# Spring Boot fat JAR: tên thường có version; dùng *.jar cho đơn giản
COPY target/*.jar app.jar

# --- Metadata: app lắng nghe 8080 (KHÔNG tự mở port ra máy host) ---
EXPOSE 8080

# --- Process chính của container = chạy Spring Boot ---
ENTRYPOINT ["java", "-jar", "app.jar"]
