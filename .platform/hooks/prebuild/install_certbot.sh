#!/bin/bash
# beanstalk EC2 재생성되면 certbot, let's encrypt 삭제돼서 다시 인증서 만들어야 함

# certbot 설치 여부 확인
if [ ! -d /etc/letsencrypt/live ]; then
    echo "Certbot이 설치되어 있지 않아 설치를 진행합니다..."

    # certbot 설치
    sudo yum install -y certbot
    sudo yum install -y python3-certbot-nginx

    # 인증서 발급
    sudo certbot certonly --webroot \
        -w /usr/share/nginx/html \
        -d dldm.kr \
        --email kimapbel@gmail.com \
        --agree-tos \
        --no-eff-email \
        --non-interactive

    # 인증서 갱신 후 Nginx 재시작 스크립트 생성
    sudo tee /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh > /dev/null <<'EOF'
#!/bin/bash
echo "인증서 갱신 후 Nginx를 재시작합니다..."
/usr/sbin/nginx -s reload
EOF

    sudo chmod +x /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh

    # Nginx 재시작
    sudo systemctl restart nginx
else
    echo "Certbot이 이미 설치되어 있습니다."
fi
