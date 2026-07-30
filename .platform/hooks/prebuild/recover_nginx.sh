#!/bin/bash

# prebuild 단계에서 systemd가 nginx를 자동으로 시작하려는데, 이미 nignx가 떠있는 경우 때문에
# sudo 붙이면 에러남
if  systemctl is-active --quiet nginx; then
    echo "[INFO] nginx is active. so skip to restart nginx"
else
    echo "[WARN] nginx is not active. try to start nginx"
    #systemctl이 아닌 무언가에 의해 nginx가 이미 실행되는 경우 때문에 pkill 하는건데
    #pkill하면 beanstalk 부팅 종료됨
    ##pkill -f nginx || true
    systemctl start nginx
    echo "[INFO] nginx restart successfully"
fi