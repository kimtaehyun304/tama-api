#!/bin/bash

#nginx.conf overwrite 이후 적용하기 위해
#overwrite 전,후 가 같으면 불필요하긴 함
#인증서 자동 갱신후 실행됨
echo "[INFO] reload nginx"
sudo nginx -s reload