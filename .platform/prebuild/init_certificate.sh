#!/bin/bash
set -e

echo command -v certbot >/dev/null 2>&1

# certbot 설치 여부 확인
if ! command -v certbot >/dev/null 2>&1; then
    echo "Certbot not installed. installing..."
    yum install -y certbot python3-certbot-nginx

    # nginx.conf 자동 적용 + 인증서 발급
    certbot --nginx \
        -d dldm.kr \
        --email kimapbel@gmail.com \
        --agree-tos \
        --no-eff-email \
        --non-interactive
else
    echo "Certbot is already installed, skipping installation."
fi

# 인증서 갱신 후 Nginx 재시작/시작해주는 스크립트
cat <<'EOF' > /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh
#!/bin/bash

# systemd가 nginx를 자동으로 시작하려는데, 이미 nginx 프로세스가 떠 있는 경우를 대비
if ! systemctl is-active --quiet nginx; then
    echo "[WARN] Nginx is not active. try to restart nginx..."
    pkill -f nginx || true
    systemctl start nginx
    echo "[SUCCESS] Nginx restart successfully."
fi
EOF

chmod +x /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh
