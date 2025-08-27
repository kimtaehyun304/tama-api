#!/bin/bash
set -e

# 포트 범위 늘리기
echo "10240 65535" > /proc/sys/net/ipv4/ip_local_port_range

# tcp 처리량 늘리기 (톸캣 max-connections에 맞춤)
sudo sysctl -w net.core.somaxconn=8192