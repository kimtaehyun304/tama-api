#!/bin/bash
set -e

# Pinpoint Agent 설치 경로
AGENT_DIR="/home/ec2-uer/pinpoint-agent-2.5.1"
PINPOINT_AGENT_ID="tama-agent"
PINPOINT_APP_NAME="tama"
PINPOINT_COLLECTOR_IP="43.202.5.63"

# 이미 있으면 스킵
if [ ! -d "$AGENT_DIR" ]; then
  mkdir -p "$AGENT_DIR"
  curl -L https://github.com/pinpoint-apm/pinpoint/releases/download/v2.5.1/pinpoint-agent-2.5.1.tar.gz \
    -o /tmp/pinpoint-agent.tar.gz
  tar -xzf /tmp/pinpoint-agent.tar.gz -C "$AGENT_DIR" --strip-components=1
  rm /tmp/pinpoint-agent.tar.gz
fi

# 환경변수 설정
export JAVA_OPTS="$JAVA_OPTS \
-javaagent:$AGENT_DIR/pinpoint-bootstrap.jar \
-Dpinpoint.agentId=${PINPOINT_AGENT_ID} \
-Dpinpoint.applicationName=${PINPOINT_APP_NAME} \
-Dpinpoint.collector.ip=${PINPOINT_COLLECTOR_IP}"
