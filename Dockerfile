# 빌드 스테이지
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Kotlin/Gradle 런타임 파일 위치를 /tmp로 바꾸기 (root 홈 대신)
ENV KOTLIN_USER_HOME=/tmp/.kotlin

# (선택) Gradle 캐시 위치를 작업 디렉토리로 고정하면 CI에서 히트가 쉬움
# ENV GRADLE_USER_HOME=/app/.gradle

# Gradle 래퍼/설정 먼저 복사 (레이어 캐시 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 복사
COPY src src

# 실행 권한 + 빌드
# 1) Gradle Daemon 비활성화
# 2) Kotlin Compiler Daemon 비활성화(= in-process 실행)
# 3) 같은 RUN 안에서 Kotlin daemon 잔여물 제거 (레이어 스냅샷 전에 정리)
RUN set -eux; \
    chmod +x ./gradlew; \
    ./gradlew build -x test --no-daemon -Dkotlin.compiler.execution.strategy=in-process; \
    rm -rf "${KOTLIN_USER_HOME}/daemon" || true

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
