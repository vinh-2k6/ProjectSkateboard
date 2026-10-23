
# Stage 1: Build Spring Boot application
# Lấy môi trường JDK 25 để build ứng dụng.
# AS build đặt tên cho stage này là "build".
FROM eclipse-temurin:25-jdk AS build

# Đặt thư mục làm việc hiện tại là /app
WORKDIR /app

# Sao chép nội dung build context (thư mục demo) vào /app
COPY . .

# Cấp quyền thực thi cho Maven Wrapper, sau đó build ứng dụng thành file JAR.
# ./mvnw: chạy Maven Wrapper trong thư mục hiện tại.
# -DskipTests: bỏ qua việc chạy test.
# package: biên dịch và đóng gói ứng dụng thành file JAR trong target/.
# Thư mục demo không bị biến mất; Maven tạo thêm file JAR trong target/.
RUN chmod +x mvnw && ./mvnw -DskipTests package


# Stage 2: Run application
# Dùng JRE 25 để chạy ứng dụng, không cần môi trường JDK đầy đủ.
FROM eclipse-temurin:25-jre

# Đặt thư mục làm việc của stage 2 là /app.
WORKDIR /app

# Sao chép file JAR từ stage có tên "build" sang stage hiện tại.
# /app/target/*.jar: file JAR được tạo ở stage 1.
# app.jar: tên file đích tại /app/app.jar trong stage 2.
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Khai báo cổng mà ứng dụng sử dụng.
EXPOSE 8090

# Lệnh khởi chạy ứng dụng khi container bắt đầu chạy.
ENTRYPOINT ["java", "-jar", "app.jar"]
