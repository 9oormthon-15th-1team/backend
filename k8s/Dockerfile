# 빌드 스테이지
FROM eclipse-temurin:17-jdk-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 래퍼와 빌드 파일들 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# 실행 권한 부여 및 빌드 (테스트 제외)
RUN chmod +x ./gradlew
RUN ./gradlew build -x test --no-daemon

# 런타임 스테이지
FROM eclipse-temurin:17-jre-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080 8081

# JVM 메모리 옵션 설정
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
