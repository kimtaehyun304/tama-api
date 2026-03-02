#!/bin/bash

# systemd가 nginx를 자동으로 시작하려는데, 이미 nignx가 떠있는 경우 때문에
if  systemctl is-active --quiet nginx; then
    echo "[INFO] nginx is active. so skip to restart nginx"
else
    echo "[WARN] nginx is not active. try to start nginx"
    sudo pkill -f nginx
    sudo systemctl start nginx
    echo "[INFO] nginx restart successfully"
fi    