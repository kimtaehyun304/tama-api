# 베이스 이미지
FROM amazonlinux:2023

WORKDIR /app

# 필요한 패키지 설치 (wget, tar, procps, bash, bash-completion, curl)
RUN dnf install -y java-17-amazon-corretto wget tar procps curl && \ dnf clean all

# Pinpoint Agent 다운로드 및 설정
RUN wget https://github.com/pinpoint-apm/pinpoint/releases/download/v2.5.1/pinpoint-agent-2.5.1.tar.gz && \
    tar -zxvf pinpoint-agent-2.5.1.tar.gz && \
    rm pinpoint-agent-2.5.1.tar.gz && \
    sed -i "s|profiler.transport.grpc.collector.ip=.*|profiler.transport.grpc.collector.ip=43.202.5.63|" \
        /app/pinpoint-agent-2.5.1/profiles/release/pinpoint.config && \
    sed -i "s|profiler.sampling.counting.sampling-rate=.*|profiler.sampling.counting.sampling-rate=1|" \
        /app/pinpoint-agent-2.5.1/profiles/release/pinpoint.config

# 애플리케이션 JAR 복사
COPY application.jar ./

EXPOSE 5000

# Pinpoint Agent와 함께 실행 (bash 환경 적용)
CMD ["java", \
     "-javaagent:pinpoint-agent-2.5.1/pinpoint-bootstrap.jar", \
     "-Dpinpoint.agentId=tama-agent", \
     "-Dpinpoint.applicationName=tama", \
     "-jar", "application.jar"]