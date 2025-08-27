#!/bin/bash
set -e

# 포트 범위 늘리기
echo "10240 65535" > /proc/sys/net/ipv4/ip_local_port_range