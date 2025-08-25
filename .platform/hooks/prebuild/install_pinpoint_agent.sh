#!/bin/bash

# 디렉토리 체크: []와 경로 사이에 공백 필요
if [ ! -d /home/ec2-user/pinpoint-agent-2.5.1 ]; then

    # Pinpoint Agent 다운로드
    wget https://github.com/pinpoint-apm/pinpoint/releases/download/v2.5.1/pinpoint-agent-2.5.1.tar.gz

    # 압축 풀기
    tar -zxvf pinpoint-agent-2.5.1.tar.gz -C /home/ec2-user/

    # 설정파일 경로
    CONFIG_PATH="/home/ec2-user/pinpoint-agent-2.5.1/profiles/release/pinpoint.config"

    MONITORING_SERVER_IP="43.202.5.63"

    # 모니터링 서버 IP 변경
    # 여기서 {ip} 부분을 실제 Collector 서버 IP로 바꿔야 함
    sed -i "s|profiler.transport.grpc.collector.ip=.*|profiler.transport.grpc.collector.ip=$MONITORING_SERVER_IP|" "$CONFIG_PATH"

fi
