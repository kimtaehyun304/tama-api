#!/bin/bash
PINPOINT_DIRECTORY="/var/app/current/pinpoint-agent-2.5.1"
# 디렉토리 체크: []와 경로 사이에 공백 필요
if [ ! -d PINPOINT_DIRECTORY ]; then

    # Pinpoint Agent 다운로드
    wget https://github.com/pinpoint-apm/pinpoint/releases/download/v2.5.1/pinpoint-agent-2.5.1.tar.gz

    # 압축 풀기 (beanstalk의 application.jar가 있는 곳)
    tar -zxvf pinpoint-agent-2.5.1.tar.gz -C /var/app/current

    # 설정파일 경로
    CONFIG_PATH="$PINPOINT_DIRECTORY/profiles/release/pinpoint.config"

    MONITORING_SERVER_IP="43.202.5.63"

    # 모니터링 서버 IP 변경
    # 여기서 {ip} 부분을 실제 Collector 서버 IP로 바꿔야 함
    sed -i "s|profiler.transport.grpc.collector.ip=.*|profiler.transport.grpc.collector.ip=$MONITORING_SERVER_IP|" "$CONFIG_PATH"

fi
