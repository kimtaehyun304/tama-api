#!/bin/bash
set -e

# 포트 범위 늘리기 (덤으로 ulimit도 증가함)
echo "10240 65535" > /proc/sys/net/ipv4/ip_local_port_range

# tcp 처리량 늘리기
sudo sysctl -w net.core.somaxconn=1024
sudo sysctl net.ipv4.tcp_max_syn_backlog=1024
