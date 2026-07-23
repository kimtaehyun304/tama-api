#!/bin/bash
# Beanstalk EC2 재생성 시 Certbot 설치 + 인증서 발급 + 자동 갱신 설정
# 인증서만 지워지는 경우도 있음

set -e

DOMAIN="dldm.kr"
EMAIL="kimapbel@gmail.com"

# 1. Certbot 없으면 설치
if ! command -v certbot >/dev/null 2>&1; then
    echo "[INFO] Installing Certbot..."
    yum install -y certbot python3-certbot-nginx
else
    echo "[INFO] Certbot already installed."
fi

# 2. 인증서가 없으면 발급
if [ ! -d "/etc/letsencrypt/live" ] || [ ! -d "/etc/letsencrypt/live/${DOMAIN}" ]; then
    echo "[INFO] Certificate not found. Issuing certificate..."

    certbot --nginx \
        -d "$DOMAIN" \
        --email "$EMAIL" \
        --agree-tos \
        --no-eff-email \
        --non-interactive

    systemctl reload nginx
    systemctl enable --now certbot-renew.timer
else
    echo "[INFO] Certificate already exists."
fi

# 3. 갱신 후 nginx 처리 Hook 생성
mkdir -p /etc/letsencrypt/renewal-hooks/deploy

cat <<'EOF' > /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh
#!/bin/bash

# nginx가 실행 중이면 reload
if ! systemctl reload nginx; then
    echo "[WARN] Reload failed. Restarting nginx..."
    pkill -f nginx || true
    systemctl start nginx
fi
EOF

chmod +x /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh

echo "[INFO] SSL setup completed."