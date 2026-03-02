#!/bin/bash
set -e

# live/도메인 디렉토리는 있는데, 인증서만 지워진 경우가 있음
if [ ! -f /etc/letsencrypt/live/README ]; then
  echo "[WARN] try to recover certificate..."
   #nignx.conf 자동 적용
   certbot --nginx \
        -d dldm.kr \
        --email kimapbel@gmail.com \
        --agree-tos \
        --no-eff-email \
        --non-interactive
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
