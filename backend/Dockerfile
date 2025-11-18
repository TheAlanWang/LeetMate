FROM eclipse-temurin:21-jdk AS build

# 安装 Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean

WORKDIR /workspace

COPY backend/ /workspace/backend/

WORKDIR /workspace/backend

# 构建
RUN mvn clean package -q -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/backend/target/*.jar /app/app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java","-jar","/app/app.jar"]