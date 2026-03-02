#!/bin/bash

# systemd가 nginx를 자동으로 시작하려는데, 이미 nignx가 떠있는 경우 때문에
if ! systemctl is-active nginx; then
    echo "[WARN] Nginx is not active. try to kill and restart nginx..."
    sudo pkill -f nginx
    sudo systemctl start nginx
    echo "[SUCCESS] Nginx kill and restart successfully."
fi
echo "systemctl status nginx is active"