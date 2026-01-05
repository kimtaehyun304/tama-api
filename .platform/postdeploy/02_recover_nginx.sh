#!/bin/bash

# systemd가 nginx를 자동으로 시작하려는데, 이미 nignx가 떠있는 경우 때문에
if ! systemctl is-active --quiet nginx; then
    echo "[WARN] Nginx is not active. try to restart nginx..."
    pkill -f nginx
    systemctl start nginx
    echo "[SUCCESS] Nginx restart successfully."
fi