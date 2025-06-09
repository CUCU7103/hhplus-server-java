# ┌────────────────────────────────────────────────────────────────────────┐
# │ Stage 1: Build Stage                                                  │
# │ - Gradle Wrapper가 git 명령을 호출하므로, Alpine 컨테이너에 git을 설치 │
# └────────────────────────────────────────────────────────────────────────┘
FROM openjdk:17-jdk-alpine AS builder
WORKDIR /app

# Alpine 패키지 인덱스를 업데이트하고 Git을 설치
RUN apk update && apk add --no-cache git

# 프로젝트 전체를 복사
COPY . /app

# gradlew 실행 권한 부여 후, 테스트를 제외하고 빌드
RUN chmod +x ./gradlew && ./gradlew clean build -x test

# ┌────────────────────────────────────────────────────────────────────────┐
# │ Stage 2: Runtime Stage                                                 │
# │ - JRE만 포함된 이미지로 빌드 결과(JAR)만 복사하여 실행 환경 구성        │
# └────────────────────────────────────────────────────────────────────────┘
FROM openjdk:17-alpine
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일을 복사
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# 컨테이너가 리스닝할 포트(예: 8082)와 ENTRYPOINT 설정
EXPOSE 8082
ENTRYPOINT ["java", "-Dserver.port=8082", "-jar", "app.jar"]
