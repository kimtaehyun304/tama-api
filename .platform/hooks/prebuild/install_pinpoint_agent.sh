#!/bin/bash
PINPOINT_DIRECTORY="/home/ec2-user/pinpoint-agent-2.5.1"
LOGFILE=/var/log/pinpoint-prestart.log
# 디렉토리 체크: []와 경로 사이에 공백 필요
if [ ! -d "$PINPOINT_DIRECTORY" ]; then

    cd /home/ec2-user

    # Pinpoint Agent 다운로드
    wget https://github.com/pinpoint-apm/pinpoint/releases/download/v2.5.1/pinpoint-agent-2.5.1.tar.gz

    # 압축 풀기 (beanstalk의 application.jar가 있는 곳)
    tar -zxvf --no-same-owner pinpoint-agent-2.5.1.tar.gz >> $LOGFILE 2>&1 || {
        echo "Failed to extract Pinpoint Agent!" >> $LOGFILE 2>&1
        exit 1
    }

    sudo rm -rf pinpoint-agent-2.5.1.tar.gz;

    # 설정파일 경로
    CONFIG_PATH="pinpoint-agent-2.5.1/profiles/release/pinpoint.config"

    MONITORING_SERVER_IP="43.202.5.63"

    # 모니터링 서버 IP 변경
    # 여기서 {ip} 부분을 실제 Collector 서버 IP로 바꿔야 함
    sed -i "s|profiler.transport.grpc.collector.ip=.*|profiler.transport.grpc.collector.ip=$MONITORING_SERVER_IP|" "$CONFIG_PATH"

fi
