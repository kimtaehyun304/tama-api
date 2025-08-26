# 베이스 이미지
FROM openjdk:17-jdk-slim

WORKDIR /app

# 필요한 패키지 설치 (wget, tar, procps, bash, bash-completion, curl)
RUN apt-get update && apt-get install -y wget tar procps bash bash-completion curl

# Docker CLI bash-completion 설치
RUN curl -L https://raw.githubusercontent.com/docker/cli/master/contrib/completion/bash/docker/etc/bash_completion/docker.sh

# bash-completion 적용
RUN echo "if ! shopt -oq posix; then" >> /etc/bash.bashrc && \
    echo "  if [ -f /usr/share/bash-completion/bash_completion ]; then" >> /etc/bash.bashrc && \
    echo "    . /usr/share/bash-completion/bash_completion" >> /etc/bash.bashrc && \
    echo "  elif [ -f /etc/bash_completion ]; then" >> /etc/bash.bashrc && \
    echo "    . /etc/bash_completion" >> /etc/bash.bashrc && \
    echo "  fi" >> /etc/bash.bashrc && \
    echo "fi" >> /etc/bash.bashrc


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
CMD ["bash", "-c", "source /etc/bash.bashrc && java -javaagent:pinpoint-agent-2.5.1/pinpoint-bootstrap.jar -Dpinpoint.agentId=tama-agent -Dpinpoint.applicationName=tama -jar application.jar"]