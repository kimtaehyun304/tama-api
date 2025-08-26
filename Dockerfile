# 베이스 이미지
FROM openjdk:17-jdk-slim

WORKDIR /app

# 필요한 패키지 설치
RUN apt-get update && apt-get install -y wget tar

# Pinpoint Agent 다운로드 및 설정파일 수정 (이미 있으면 스킵)
# 모니터링 ip 지정, 트래픽 전부 기록
RUN if [ ! -d /app/pinpoint-agent-2.5.1 ]; then \
        wget https://github.com/pinpoint-apm/pinpoint/releases/download/v2.5.1/pinpoint-agent-2.5.1.tar.gz && \
        tar -zxvf pinpoint-agent-2.5.1.tar.gz && \
        rm pinpoint-agent-2.5.1.tar.gz && \
        sed -i "s|profiler.transport.grpc.collector.ip=.*|profiler.transport.grpc.collector.ip=43.202.5.63|" \
            /app/pinpoint-agent-2.5.1/profiles/release/pinpoint.config && \
        sed -i "s|profiler.sampling.rate=.*|profiler.sampling.rate=1|" \
            /app/pinpoint-agent-2.5.1/profiles/release/pinpoint.config; \
    fi

# 애플리케이션 JAR 복사
COPY application.jar ./

EXPOSE 5000

# Pinpoint Agent와 함께 실행
CMD ["java", \
     "-javaagent:pinpoint-agent-2.5.1/pinpoint-bootstrap.jar", \
     "-Dpinpoint.agentId=tama-agent", \
     "-Dpinpoint.applicationName=tama", \
     "-jar", "application.jar"]
