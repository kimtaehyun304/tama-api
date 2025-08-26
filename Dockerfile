# 베이스 이미지
FROM openjdk:17-jdk-slim

WORKDIR /app

# 필요한 패키지 설치
RUN apt-get update && apt-get install -y wget tar

# Pinpoint Agent 다운로드 및 압축 풀기
RUN wget https://github.com/pinpoint-apm/pinpoint/releases/download/v2.5.1/pinpoint-agent-2.5.1.tar.gz && \
    tar -zxvf pinpoint-agent-2.5.1.tar.gz && \
    rm pinpoint-agent-2.5.1.tar.gz

# 설정파일 IP 변경
RUN sed -i "s|profiler.transport.grpc.collector.ip=.*|profiler.transport.grpc.collector.ip=43.202.5.63|" \
    /app/pinpoint-agent-2.5.1/profiles/release/pinpoint.config

# 애플리케이션 JAR 복사
COPY application.jar ./

# Pinpoint Agent와 함께 실행
CMD ["java", \
     "-javaagent:pinpoint-agent-2.5.1/pinpoint-bootstrap.jar", \
     "-Dpinpoint.agentId=tama-agent", \
     "-Dpinpoint.applicationName=tama", \
     "-jar", "application.jar"]
