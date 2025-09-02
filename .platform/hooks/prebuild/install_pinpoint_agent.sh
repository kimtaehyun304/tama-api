#!/bin/bash
# PINPOINT_DIRECTORY="/home/ec2-user/pinpoint-agent-2.5.1"
PINPOINT_DIRECTORY="/var/app/current/pinpoint-agent-2.5.1"
LOGFILE=/var/log/pinpoint-prestart.log
if [ ! -d "$PINPOINT_DIRECTORY" ]; then

    cd /var/app/current

    # Pinpoint Agent 다운로드
    wget https://github.com/pinpoint-apm/pinpoint/releases/download/v2.5.1/pinpoint-agent-2.5.1.tar.gz

    # 압축 풀기
    tar -zxvf pinpoint-agent-2.5.1.tar.gz >> $LOGFILE 2>&1 || {
        echo "Failed to extract Pinpoint Agent!" >> $LOGFILE 2>&1
        exit 1
    }

    sudo rm -rf pinpoint-agent-2.5.1.tar.gz;

    # 설정파일 경로
    CONFIG_PATH="pinpoint-agent-2.5.1/profiles/release/pinpoint.config"

    MONITORING_SERVER_IP="43.202.5.63"

    # 모니터링 서버 IP 변경
    sed -i "s|profiler.transport.grpc.collector.ip=.*|profiler.transport.grpc.collector.ip=$MONITORING_SERVER_IP|" "$CONFIG_PATH"

fi
