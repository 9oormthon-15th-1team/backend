# 빌드 스테이지
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Kotlin/Gradle 런타임 파일 위치를 /tmp로 바꾸기 (root 홈 대신)
ENV KOTLIN_USER_HOME=/tmp/.kotlin
ENV GRADLE_USER_HOME=/app/.gradle

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
# 3) 빌드 후 모든 daemon 관련 파일 완전 제거
RUN set -eux; \
    chmod +x ./gradlew; \
    ./gradlew build -x test --no-daemon -Dkotlin.compiler.execution.strategy=in-process \
        -Dkotlin.daemon.useFallbackStrategy=false \
        -Dorg.gradle.jvmargs="-Xmx2048m -XX:MaxMetaspaceSize=512m"; \
    # Kotlin daemon 관련 모든 파일 제거
    rm -rf /tmp/.kotlin/daemon* || true; \
    rm -rf /root/.kotlin/daemon* || true; \
    rm -rf "${KOTLIN_USER_HOME}/daemon"* || true; \
    # Gradle 캐시에서도 daemon 관련 파일 제거
    find /app/.gradle -name "*daemon*" -type f -delete || true; \
    find /app/.gradle -name "*daemon*" -type d -exec rm -rf {} + || true; \
    # 프로세스 확인 후 강제 종료
    ps aux | grep -i kotlin | grep -v grep | awk '{print $2}' | xargs kill -9 || true; \
    # 잠시 대기 후 최종 정리
    sleep 2; \
    rm -rf /tmp/.kotlin || true

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