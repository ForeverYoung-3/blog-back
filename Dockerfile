# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
# 先复制 pom.xml，利用 Docker 缓存层加速依赖下载
COPY pom.xml .
RUN mvn dependency:go-offline -B
# 再复制源码编译
COPY src ./src
RUN mvn package -DskipTests -B

# ---- Run Stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# 从 builder 阶段复制 jar
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
